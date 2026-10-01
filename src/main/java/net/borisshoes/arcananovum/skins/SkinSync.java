package net.borisshoes.arcananovum.skins;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

final class SkinSync {
   static final ScheduledExecutorService EXEC = Executors.newSingleThreadScheduledExecutor(runnable -> {
      Thread thread = new Thread(runnable, "ArcanaNovum-Skins");
      thread.setDaemon(true);
      return thread;
   });
   private static final AtomicBoolean RUNNING = new AtomicBoolean(false);
   private static final int MAX_ROUNDS = 3;
   private static volatile CompletableFuture<Void> initial = CompletableFuture.completedFuture(null);
   static volatile @Nullable ArcanaSkinApi api;
   static final String SALT = "thestardustwesnortedalongtheway";
   
   private SkinSync(){
   }
   
   static boolean dedicated(){
      return FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER;
   }
   
   static void start() throws IOException{
      if(!ArcanaSkins.enabled()){
         ArcanaSkins.dev("skinsEnabled is false; the skin API is not called this session");
         return;
      }
      if(System.getProperty("fabric-api.datagen") != null) return; // runDatagen has no use for skins
      ArcanaSkinApi.Versions versions = ArcanaSkins.currentSkinVersions();
      ArcanaSkins.dev("Skin API {} as instance {}, mod version {}, {}, request timeout {}s, pack wait {}s, catalog refresh {}min, player refresh {}min, {}",
            ArcanaSkins.apiUrl(), SkinStore.getInstanceId(), ArcanaSkins.modVersion(), versions, ArcanaSkins.requestTimeoutSeconds(), ArcanaSkins.packWaitSeconds(),
            ArcanaSkins.catalogRefreshMinutes(), ArcanaSkins.playerRefreshMinutes(), dedicated() ? "dedicated server" : "client");
      api = new ArcanaSkinApi(ArcanaSkins.apiUrl(), SkinStore.getInstanceId(), SALT, versions, ArcanaSkins.modVersion(), Duration.ofSeconds(ArcanaSkins.requestTimeoutSeconds()));
      initial = CompletableFuture.runAsync(SkinSync::syncOnce, EXEC);
      // The client builds its pack once per launch, so later syncs there could not change anything until the next one
      long every = ArcanaSkins.catalogRefreshMinutes();
      if(dedicated() && every > 0){
         EXEC.scheduleWithFixedDelay(SkinSync::syncOnce, every, every, TimeUnit.MINUTES);
      }
   }

   static void awaitFirstSync(){
      long budget = ArcanaSkins.packWaitSeconds();
      try{
         initial.get(budget, TimeUnit.SECONDS);
      }catch(TimeoutException slow){
         ArcanaSkins.warn("pack-wait", "Skin download still running after " + budget + "s; building the resource pack with saved skins", null);
      }catch(Throwable ignored){
      }
   }
   
   static void syncOnce(){
      ArcanaSkinApi client = api;
      if(client == null || !RUNNING.compareAndSet(false, true)) return;
      try{
         for(int round = 0; round < MAX_ROUNDS; round++){
            ArcanaSkinApi.SkinList list = call(client::listSkins);
            String savedPackHash = SkinStore.currentPackHash();
            ArcanaSkins.dev("Sync round {}: API lists {} skins with pack hash {}; saved pack hash is {}", round + 1, list.skins().size(), list.packHash(), savedPackHash.isEmpty() ? "<none>" : savedPackHash);
            if(list.packHash().equals(savedPackHash)){
               ArcanaSkins.recovered("sync", "Skin API reachable again; skins are up to date");
               return;
            }
            List<String> ids = list.skins().stream().map(skin -> skin.get("id").getAsString()).toList();
            if(ids.isEmpty()){
               SkinStore.installEmpty(list.packHash());
               ArcanaSkins.recovered("sync", "Skin API reachable again");
               ArcanaSkins.info("The skin API lists no skins for this version; saved skins removed");
               return;
            }
            ArcanaSkinApi.Bundle bundle;
            try{
               bundle = call(() -> client.downloadSkins(ids));
            }catch(ArcanaSkinApi.ApiException e){
               if(e.status == 404 && "unknown_skins".equals(e.error)){
                  ArcanaSkins.dev("A skin was removed between the list and data calls ({}); listing again", e.body);
                  continue;
               }
               throw e;
            }
            ArcanaSkins.dev("Downloaded bundle {} with {} files for skins {}", bundle.hash(), bundle.files().size(), ids);
            if(!bundle.hash().equals(list.packHash())){
               ArcanaSkins.dev("Bundle hash differs from the listed pack hash {}: a skin changed between the two calls; listing again", list.packHash());
               continue;
            }
            SkinStore.install(bundle);
            ArcanaSkins.recovered("sync", "Skin API reachable again");
            ArcanaSkins.info("Downloaded {} skins; they apply the next time the resource pack is built", ids.size());
            return;
         }
         ArcanaSkins.warn("sync-race", "Skin list kept changing during download; keeping saved skins until the next pass", null);
      }catch(Throwable t){
         ArcanaSkins.warn("sync", "Could not update skins from the API; using saved skins", t);
      }finally{
         RUNNING.set(false);
      }
   }
   
   interface ApiCall<T> {
      T run() throws IOException, InterruptedException;
   }
   
   static <T> T call(ApiCall<T> request) throws IOException, InterruptedException{
      try{
         return request.run();
      }catch(ArcanaSkinApi.ApiException e){
         if(!"stale_timestamp".equals(e.error) || e.body == null || !e.body.has("server_time")) throw e;
         ArcanaSkinApi client = api;
         if(client == null) throw e;
         long offset = e.body.get("server_time").getAsLong() - System.currentTimeMillis();
         ArcanaSkins.dev("stale_timestamp: signing with a clock offset of {}ms from now on", offset);
         client.setClockOffset(offset);
         ArcanaSkins.warn("clock", "This machine's clock is off by more than 5 minutes; correcting for skin requests", null);
         return request.run();
      }
   }
}
