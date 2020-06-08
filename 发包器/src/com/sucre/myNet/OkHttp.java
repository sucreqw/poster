package com.sucre.myNet;

import okhttp3.*;

import javax.net.ssl.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class OkHttp implements X509TrustManager {



    /**
     * 重载post请求，去掉报关参数。
     *
     * @param url
     * @param body
     * @return
     */
    public String goPost(String url, HashMap<String, String> body) {
        return goPost(url, null, body);
    }

    /**
     * post 请求，报头可为空。
     *
     * @param url    连接地址 可带上端口号。
     * @param header 报头，可为空
     * @param body   key,value形式的body
     * @return
     */
    public String goPost(String url, HashMap<String, String> header, HashMap<String, String> body) {

        //先取证书和fatory
        List<Object> list=getFatory();
        //设置代理.
        //client.newBuilder().proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost",8886)));
        OkHttpClient client = new OkHttpClient();

        client.newBuilder().sslSocketFactory((SSLSocketFactory) list.get(0),(X509TrustManager)list.get(1));
        client.newBuilder().hostnameVerifier(new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });

        FormBody.Builder requestBody = new FormBody.Builder();
        //加入body
        if (body != null && body.size() != 0) {
            for (String bodys : body.keySet()) {
                requestBody.add(bodys, body.get(bodys));
            }
        }
        Request.Builder request = new Request.Builder();

        //加入报头
        if (header != null && header.size() != 0) {
            for (String head : header.keySet()) {
                request.addHeader(head, header.get(head));
            }
        }
        request.url(url);
        request.post(requestBody.build());
        //request.build();

        try (
                Response response = client.newCall(request.build()).execute()
        ) {
            return response.headers() + response.body().string();
        } catch (Exception e) {
            System.out.println("okhttp出错！" + e.getMessage());
        }


        return null;
    }

    /**
     * post 请求，body格式为joson
     * @param url
     * @param header
     * @param body
     * @return
     */
    public String goPost(String url,HashMap<String,String> header, String body){
        // 提交JSON
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), body);

        //设置代理.
        //        //client.newBuilder().proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost",8886)));
        OkHttpClient client = new OkHttpClient();

        List<Object> list=new ArrayList<>();
        list=getFatory();
        client.newBuilder().sslSocketFactory((SSLSocketFactory) list.get(0),(X509TrustManager) list.get(1));
        client.newBuilder().hostnameVerifier(new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });

        Request.Builder request = new Request.Builder();

        //加入报头
        if (header != null && header.size() != 0) {
            for (String head : header.keySet()) {
                request.addHeader(head, header.get(head));
            }
        }
        request.url(url);
        request.post(requestBody);
        //request.build();

        try (
                Response response = client.newCall(request.build()).execute()
        ) {
            return response.headers() + response.body().string();
        } catch (Exception e) {
            System.out.println("okhttp出错！" + e.getMessage());
        }


        return null;
    }
    /**
     * 重载get请求，无报头参数。
     *
     * @param url
     * @return
     */
    public String goGet(String url) {
        return goGet(url, null);
    }

    /**
     * get请求，参数可以直接定义在url里。
     *
     * @param url
     * @param header 自定义报头，可为空。
     * @return
     */
    public String goGet(String url, HashMap<String, String> header) {

        //先取证书和fatory
        List<Object> list=getFatory();
        OkHttpClient client = new OkHttpClient();
        Request.Builder request = new Request.Builder();
        client.newBuilder().sslSocketFactory((SSLSocketFactory) list.get(0),(X509TrustManager)list.get(1));
        client.newBuilder().hostnameVerifier(new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });

        //加入报头
        if (header != null && header.size() != 0) {
            for (String head : header.keySet()) {
                request.addHeader(head, header.get(head));
            }
        }
        request.url(url);
        //request.build();

        try (
                Response response = client.newCall(request.build()).execute()
        ) {
            return response.headers() + response.body().string();
        } catch (Exception e) {
            System.out.println("okhttp出错！" + e.getMessage());
        }


        return null;
    }

    private static SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;

        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new OkHttp()}, new SecureRandom());

            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
        }

        return ssfFactory;
    }
    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

    }

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }


    private List<Object> getFatory(){
        List<Object> ret=new ArrayList<>();
        TrustManagerFactory trustManagerFactory = null;
        try {
            trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            trustManagerFactory.init((KeyStore) null);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
            throw new IllegalStateException("Unexpected default trust managers:"
                    + Arrays.toString(trustManagers));
        }
        X509TrustManager trustManager = (X509TrustManager) trustManagers[0];

        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLS");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            sslContext.init(null, new TrustManager[] { trustManager }, null);
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
        ret.add(sslSocketFactory);
        ret.add(trustManager);
        return ret;
    }
}
