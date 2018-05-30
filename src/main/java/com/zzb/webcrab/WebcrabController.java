package com.zzb.webcrab;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class WebcrabController {

    final static String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.181 Safari/537.36";

    @RequestMapping(value = "/", method = RequestMethod.POST)
    ResponseEntity<ResponseWrapper> getWebBaseInfo(@RequestBody UrlParam urlParam) {
        String url = urlParam.getUrl();

        String title = "";
        String description = "";
        String imageUrl = "";
        List<String> imageUrlArray = new ArrayList<>();

        try {
            Document document = Jsoup.connect(url)
                    .userAgent(WebcrabController.USER_AGENT)
                    .timeout(50000)
                    .validateTLSCertificates(false)
                    .followRedirects(true)
                    .get();

            // title
            title = document.head().getElementsByTag("title").text();

            // description
            Elements elements = document.select("meta[name=description]");
            if (elements != null && elements.size() > 0) {
                description = elements.get(0).attr("content");
            } else {
                description = document.text();
                if (description.length() > 100) {
                    description = description.substring(0, 100);
                }
            }

            // image url
            elements = document.select("meta[property=og:image]");
            if (elements != null && elements.size() > 0) {
                imageUrl = elements.get(0).attr("content");
            }

            Elements imgs = document.getElementsByTag("img");
            final int imgTagCount = imgs.size();
            if(imgTagCount > 0) {
                if (null == imageUrl || imageUrl.isEmpty()) {
                    imageUrl = imgs.get(0).attr("abs:src");
                    if (null == imageUrl) {
                        imageUrl = imgs.get(0).attr("abs:data-original-src");
                    }
                }

                for (int i = 0; i < imgTagCount; ++i) {
                    String elemtntImageUrl = imgs.get(i).attr("abs:src");
                    if (null == elemtntImageUrl || elemtntImageUrl.isEmpty()) {
                        elemtntImageUrl = imgs.get(i).attr("abs:data-original-src");
                    }

                    // image url array of all img tag
                    imageUrlArray.add(elemtntImageUrl);
                }
            }

            Map map = new HashMap();
            map.put("title", title);
            map.put("description", description);
            map.put("requestUrl", url);
            map.put("firstImgUrl", imageUrl);
            map.put("imgUrlArray", imageUrlArray);

            return new ResponseEntity<>(new ResponseWrapper(map), HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ResponseEntity<>(new ResponseWrapper(Message.FAIL), HttpStatus.OK);
    }
}
