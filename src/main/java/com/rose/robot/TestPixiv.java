package com.rose.robot;

import io.github.rosemoe.botPlugin.NetworkUtilsKt;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.zip.GZIPInputStream;

public class TestPixiv {

    protected static StringBuilder getUserArtworks(long userId) throws Throwable {
        NetworkUtilsKt.installIfNot();
        HttpsURLConnection connection;
        Proxy proxyK = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 1080));
        connection = (HttpsURLConnection) new URL(String.format("https://www.pixiv.net/ajax/user/%s/profile/all", userId + "")).openConnection(proxyK);
        connection.setConnectTimeout(5000);
        connection.setDoInput(true);
        connection.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.105 Safari/537.36");
        connection.setRequestProperty("Accept-Language","zh-CN,zh;q=0.9");
        connection.setRequestProperty("Accept-Encoding","gzip, deflate, br");
        //connection.setRequestProperty("Connection","keep-alive");
        connection.setRequestProperty("Referer", String.format("https://www.pixiv.net/users/%s/artworks", userId + ""));
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Sec-Fetch-Dest", "empty");
        connection.setRequestProperty("Sec-Fetch-Mode", "cors");
        connection.setRequestProperty("Sec-Fetch-Site", "same-origin");
        connection.setRequestProperty("x-user-id", "29548697");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Cookie", "first_visit_datetime_pc=2020-02-17+18%3A29%3A32; p_ab_id=5; p_ab_id_2=8; p_ab_d_id=363236909; yuid_b=GJchUmA; _ga=GA1.2.412416079.1581931802; PHPSESSID=29548697_fPX1t8g5XwnSLBbg8grqk56lJ0Rm7wPi; c_type=30; a_type=0; b_type=1; module_orders_mypage=%5B%7B%22name%22%3A%22sketch_live%22%2C%22visible%22%3Atrue%7D%2C%7B%22name%22%3A%22tag_follow%22%2C%22visible%22%3Atrue%7D%2C%7B%22name%22%3A%22recommended_illusts%22%2C%22visible%22%3Atrue%7D%2C%7B%22name%22%3A%22everyone_new_illusts%22%2C%22visible%22%3Atrue%7D%2C%7B%22name%22%3A%22following_new_illusts%22%2C%22visible%22%3Atrue%7D%2C%7B%22name%22%3A%22mypixiv_new_illusts%22%2C%22visible%22%3Atrue%7D%2C%7B%22name%22%3A%22spotlight%22%2C%22visible%22%3Atrue%7D%2C%7B%22name%22%3A%22fanbox%22%2C%22visible%22%3Atrue%7D%2C%7B%22name%22%3A%22featured_tags%22%2C%22visible%22%3Atrue%7D%2C%7B%22name%22%3A%22contests%22%2C%22visible%22%3Atrue%7D%2C%7B%22name%22%3A%22user_events%22%2C%22visible%22%3Atrue%7D%2C%7B%22name%22%3A%22sensei_courses%22%2C%22visible%22%3Atrue%7D%2C%7B%22name%22%3A%22booth_follow_items%22%2C%22visible%22%3Atrue%7D%5D; login_ever=yes; adr_id=cNKhgMZQR1yAMwq9GpUfhBYWJgcbrzyW477bN6RzXGQlRHaw; __utmv=235335808.|2=login%20ever=yes=1^3=plan=normal=1^5=gender=male=1^6=user_id=29548697=1^9=p_ab_id=5=1^10=p_ab_id_2=8=1^11=lang=zh=1; privacy_policy_agreement=2; __utmz=235335808.1593962565.73.12.utmcsr=saucenao.com|utmccn=(referral)|utmcmd=referral|utmcct=/search.php; __cfduid=d08e3b64f2013bcad0df3cb81617c28901593970442; UM_distinctid=173200adcfc1c2-08ae1e027309f1-b383f66-13c680-173200add09251; CNZZDATA1276301425=1670873460-1593969672-https%253A%252F%252Fwww.pixiv.net%252F%7C1594393709; ki_s=204128%3A0.0.0.0.0%3B208879%3A0.0.0.0.0; categorized_tags=8NfvpmigcD~BU9SQkS-zU~EZQqoW9r8g~GX5cZxE2GY~IVwLyT8B6k~OEXgaiEbRa~RcahSSzeRf~_bee-JX46i~aLBjcKpvWL~b8b4-hqot7~m3EJRa33xU~pvU1D1orJa~qiO14cZMBI; _gid=GA1.2.1848925041.1596359854; ki_t=1581931893466%3B1596358687908%3B1596371648734%3B18%3B34; tag_view_ranking=RTJMXD26Ak~Lt-oEicbBr~zIv0cf5VVk~m3EJRa33xU~BtXd1-LPRH~0xsDLqCEW6~XpYOJt3r5W~jH0uD88V6F~75zhzbk0bS~zyKU3Q5L4C~tgP8r-gOe_~KkXUHnIeIl~-sp-9oh8uv~Ie2c51_4Sp~S_mZnA3sxf~_vzBwsdzth~65aiw_5Y72~a_SDCivJIS~_pwIgrV8TB~EUwzYuPRbU~oCR2Pbz1ly~qiO14cZMBI~BEa426Zwwo~Fq4K_8PGib~pzzjRSV6ZO~v3nOtgG77A~Bd2L9ZBE8q~1LN8nwTqf_~RVRPe90CVr~aLBjcKpvWL~_bee-JX46i~66pgV3BU1a~x_jB0UM4fe~XDEWeW9f9i~7WfWkHyQ76~1F9SMtTyiX~nQRrj5c6w_~05XvkINl3k~ykNnpw2uh5~w8ffkPoJ_S~K8esoIs2eW~qtVr8SCFs5~6RcLf9BQ-w~kGYw4gQ11Z~SPBwuNWwOW~fg8EOt4owo~SoxapNkN85~PwDMGzD6xn~RcahSSzeRf~CrFcrMFJzz~cbmDKjZf9z~lxfrUKMf9f~lH5YZxnbfC~gQ9f732ax3~1HuE7w0nKg~j99etNgEvu~SJK3YcGD-h~Ce-EdaHA-3~-IrOV3901X~L58xyNakWW~sKs0aPaW87~7o87GA4Ipo~HffPWSkEm-~LH9ZWWU5Vx~EGefOqA6KB~iFcW6hPGPU~jS8TGx1n0i~QviSTvyfUE~Hjx7wJwsUT~q303ip6Ui5~YTtj7aYVjL~BpbzRdPJXg~j3leh4reoN~YRDwjaiLZn~aKhT3n4RHZ~HY55MqmzzQ~Hvc3ekMyyh~0sdG-G1SOF~KN7uxuR89w~b1s-xqez0Y~W4Y5eToO0e~GNcgbuT3T-~MDpxawlkUA~G-1lNBdD_I~Cr3jSW1VoH~LMpjieSVIv~rqvM6GS14_~PXUg_wpf1o~Te-Gu2wMLd~-O-NE-glI0~HddulGYwn1~kHJk-sR8-P~ojUG7gl5F-~FRbD_pAEqO~gVfGX_rH_Y~ZXRBqRlFWu~aQ6UpadpGf~r43z9WTbmR~-pq5Nu4OQE~MSNRmMUDgC; __utma=235335808.412416079.1581931802.1596371648.1596440541.86; __utmc=235335808; __utmt=1; __utmb=235335808.3.10.1596440541");
        connection.connect();
        if (connection.getContentEncoding().equals("gzip")) {
            return new StringBuilder(new String(decompressGzip(connection.getInputStream())));
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(),connection.getContentEncoding() == null ? "UTF8" : connection.getContentEncoding()));
        StringBuilder sb = new StringBuilder();
        String line;
        while((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        return sb;
    }

    public static byte[] decompressGzip(InputStream is) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            GZIPInputStream ungzip = new GZIPInputStream(is);
            byte[] buffer = new byte[8192];
            int n;
            while ((n = ungzip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        byte[] result = out.toByteArray();
        out.close();
        return result;
    }

    public static void main(String[] args) throws Throwable {
        StringBuilder src = getUserArtworks(4357590L);
        System.out.println(src.toString());
    }

}
