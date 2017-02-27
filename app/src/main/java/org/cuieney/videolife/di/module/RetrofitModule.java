package org.cuieney.videolife.di.module;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.cuieney.videolife.common.api.UrlManager;
import org.cuieney.videolife.common.okhttp.CacheInterceptor;
import org.cuieney.videolife.common.okhttp.CookiesManager;
import org.cuieney.videolife.common.utils.AppConfig;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.inject.Singleton;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import dagger.Module;
import dagger.Provides;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


@Module
public class RetrofitModule {
    private final Context context;

    public RetrofitModule(Context context) {
        this.context = context;
    }

    @Singleton
    @Provides
    public Retrofit providesRetrofit(Gson gson) {
        return new Retrofit.Builder()
                .baseUrl(UrlManager.API_URL)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    @Provides
    public Gson provideGson() {
        return new GsonBuilder().
                serializeNulls().
                create();
    }




    @Provides
    public OkHttpClient provideOkhttpClient(Cache cache, CacheInterceptor cacheInterceptor, CookiesManager cookiesManager) {

        X509TrustManager xtm = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                X509Certificate[] x509Certificates = new X509Certificate[0];
                return x509Certificates;
            }
        };

        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("SSL");

            sslContext.init(null, new TrustManager[]{xtm}, new SecureRandom());

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        HostnameVerifier DO_NOT_VERIFY = (hostname, session) -> true;

        return new OkHttpClient.Builder()
                .cache(cache)//添加缓存
                .addInterceptor(cacheInterceptor)
                .sslSocketFactory(sslContext.getSocketFactory())
                .hostnameVerifier(DO_NOT_VERIFY)
                .cookieJar(cookiesManager)
                .build();

    }

    @Provides
    public CacheInterceptor providesCacheInterceptor() {
        return new CacheInterceptor(context);
    }


    @Provides
    public Cache provideCache() {
        return new Cache(context.getExternalFilesDir(AppConfig.DEFAULT_JOSN_CACHE), AppConfig.DEFAULT_CACHE_SIZE);
    }

    @Provides
    public CookiesManager providesCookies() {
        return new CookiesManager();
    }



}