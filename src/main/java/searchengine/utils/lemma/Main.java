package searchengine.utils.lemma;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;

public class Main {
    @Value(value = "${title}")
    private static Double title;
        static String content = "<html>\n" +
                "  <head>\n" +
                "   <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n" +
                "   <meta charset=\"utf-8\">\n" +
                "   <title>С Днем Рождения поздравляем актера \"Et Cetera\" Данила Никитина</title>\n" +
                "   <meta content=\"Dynamic\" name=\"document-state\"><!--<meta name=\"viewport\" content=\"width=device-width, height=device-height, initial-scale=1.0, user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\">-->\n" +
                "   <meta name=\"viewport\" content=\"width=device-width, height=device-height, initial-scale=1.0, user-scalable=1\">\n" +
                "   <link href=\"http://et-cetera.ru/\" rel=\"canonical\">\n" +
                "   <link href=\"/mobile/_img/favicon.ico\" rel=\"icon\" type=\"image/png\">\n" +
                "   <link href=\"/mobile/_img/apple-touch-icon_142x142.png\" rel=\"apple-touch-icon\" type=\"image/png\">\n" +
                "   <script type=\"text/javascript\" src=\"/mobile/_js/global_mobile.js\"></script>\n" +
                "   <script src=\"/mobile/_js/watch.js\" async type=\"text/javascript\"></script>\n" +
                "   <script src=\"/mobile/_js/analytics.js\" async></script>\n" +
                "   <script async type=\"text/javascript\" src=\"/mobile/_js/context.js\"></script><!--<script type=\"text/javascript\" src=\"/mobile/_js/adriver.js\"></script>-->\n" +
                "   <script type=\"text/javascript\" src=\"/mobile/_js/functions.js?2023\"></script>\n" +
                "   <link href=\"/mobile/_css/mobile.css?20201216\" rel=\"stylesheet\" media=\"all\"><!-- fancybox -->\n" +
                "   <link href=\"/_css/jquery.fancybox.css\" type=\"text/css\" rel=\"stylesheet\">\n" +
                "   <script type=\"text/javascript\" src=\"/_js/jquery.fancybox.pack.js\"></script>\n" +
                "   <style>\n" +
                " .fancybox-nav span {\n" +
                "  visibility: visible;\n" +
                " }\n" +
                " </style>\n" +
                "   <script>\n" +
                " $(document).ready(function() {\n" +
                " \tif ($(\".photoGallery\").length > 0) {\n" +
                " \t\t$('.photoGallery a').fancybox({\n" +
                " \t\t\tpadding : 1,\n" +
                " \t\t\tmargin : 15\n" +
                " \t\t});\n" +
                " \t}\n" +
                " });\n" +
                " </script><!-- /fancybox --> <!-- Google Tag Manager -->\n" +
                "   <script>(function(w,d,s,l,i){w[l]=w[l]||[];w[l].push({'gtm.start':\n" +
                " new Date().getTime(),event:'gtm.js'});var f=d.getElementsByTagName(s)[0],\n" +
                " j=d.createElement(s),dl=l!='dataLayer'?'&l='+l:'';j.async=true;j.src=\n" +
                " 'https://www.googletagmanager.com/gtm.js?id='+i+dl;f.parentNode.insertBefore(j,f);\n" +
                " })(window,document,'script','dataLayer','GTM-TK6P9QX');</script><!-- End Google Tag Manager --> <!-- Global site tag (gtag.js) - Google Analytics -->\n" +
                "   <script async src=\"https://www.googletagmanager.com/gtag/js?id=UA-56085942-1\"></script>\n" +
                "   <script>\n" +
                "   window.dataLayer = window.dataLayer || [];\n" +
                "   function gtag(){dataLayer.push(arguments);}\n" +
                "   gtag('js', new Date());\n" +
                " \n" +
                " gtag('config', 'UA-56085942-1', {\n" +
                "    'custom_map': {\n" +
                "      'dimension1': 'clientId',\n" +
                "    }\n" +
                " });\n" +
                " setTimeout(function(){\n" +
                " gtag('event', location.pathname, {\n" +
                "   'event_category': 'Новый посетитель'\n" +
                " });\n" +
                " }, 15000);\n" +
                " </script><!-- postmaster.mail.ru -->\n" +
                "   <meta name=\"mailru-verification\" content=\"0c0e479d0c7b0528\"><!-- /postmaster.mail.ru --> <!-- intickets.ru -->\n" +
                "   <link rel=\"stylesheet\" href=\"//s3.intickets.ru/intickets.min.css\">\n" +
                "   <script src=\"//s3.intickets.ru/intickets.min.js\"></script><!-- /intickets.ru --> <!-- Facebook Pixel Code -->\n" +
                "   <script>\n" +
                " !function(f,b,e,v,n,t,s)\n" +
                " {if(f.fbq)return;n=f.fbq=function(){n.callMethod?\n" +
                " n.callMethod.apply(n,arguments):n.queue.push(arguments)};\n" +
                " if(!f._fbq)f._fbq=n;n.push=n;n.loaded=!0;n.version='2.0';\n" +
                " n.queue=[];t=b.createElement(e);t.async=!0;\n" +
                " t.src=v;s=b.getElementsByTagName(e)[0];\n" +
                " s.parentNode.insertBefore(t,s)}(window,document,'script',\n" +
                " 'https://connect.facebook.net/en_US/fbevents.js');\n" +
                "  fbq('init', '442473676182880'); \n" +
                " fbq('track', 'PageView');\n" +
                " fbq('track', 'ViewContent');\n" +
                " fbq('track', 'AddToCart');\n" +
                " </script>\n" +
                "   <noscript>&lt;img height=\"1\" width=\"1\" src=\"https://www.facebook.com/tr?id=442473676182880&amp;amp;ev=PageView &amp;amp;noscript=1\"&gt;\n" +
                "   </noscript><!-- End Facebook Pixel Code --> <!-- Facebook Pixel Code -->\n" +
                "   <script>\n" +
                "    !function(f,b,e,v,n,t,s)\n" +
                "    {if(f.fbq)return;n=f.fbq=function(){n.callMethod?\n" +
                "    n.callMethod.apply(n,arguments):n.queue.push(arguments)};\n" +
                "    if(!f._fbq)f._fbq=n;n.push=n;n.loaded=!0;n.version='2.0';\n" +
                "    n.queue=[];t=b.createElement(e);t.async=!0;\n" +
                "    t.src=v;s=b.getElementsByTagName(e)[0];\n" +
                "    s.parentNode.insertBefore(t,s)}(window, document,'script',\n" +
                "    'https://connect.facebook.net/en_US/fbevents.js');\n" +
                "    fbq('init', '1547161622006603');\n" +
                "    fbq('track', 'PageView');\n" +
                "    fbq('track', 'ViewContent');\n" +
                "    fbq('track', 'AddToCart');\n" +
                " </script>\n" +
                "   <noscript>\n" +
                "    &lt;img height=\"1\" width=\"1\" style=\"display:none\" src=\"https://www.facebook.com/tr?id=1547161622006603&amp;amp;ev=PageView&amp;amp;noscript=1\"&gt;\n" +
                "   </noscript><!-- End Facebook Pixel Code --> <!-- Facebook Pixel Code -->\n" +
                "   <script>\n" +
                " \t!function(f,b,e,v,n,t,s)\n" +
                " \t{if(f.fbq)return;n=f.fbq=function(){n.callMethod?\n" +
                " \tn.callMethod.apply(n,arguments):n.queue.push(arguments)};\n" +
                " \tif(!f._fbq)f._fbq=n;n.push=n;n.loaded=!0;n.version='2.0';\n" +
                " \tn.queue=[];t=b.createElement(e);t.async=!0;\n" +
                " \tt.src=v;s=b.getElementsByTagName(e)[0];\n" +
                " \ts.parentNode.insertBefore(t,s)}(window, document,'script',\n" +
                " \t'https://connect.facebook.net/en_US/fbevents.js');\n" +
                " \tfbq('init', '323731701614932');\n" +
                " \tfbq('track', 'PageView');\n" +
                " \tfbq('track', 'ViewContent');\n" +
                " \tfbq('track', 'AddToCart');\n" +
                "   </script>\n" +
                "   <noscript>\n" +
                "    &lt;img height=\"1\" width=\"1\" style=\"display:none\" src=\"https://www.facebook.com/tr?id=323731701614932&amp;amp;ev=PageView&amp;amp;noscript=1\"&gt;\n" +
                "   </noscript><!-- End Facebook Pixel Code --> <!-- Facebook Pixel Code -->\n" +
                "   <script>\n" +
                " !function(f,b,e,v,n,t,s)\n" +
                " {if(f.fbq)return;n=f.fbq=function(){n.callMethod?\n" +
                " n.callMethod.apply(n,arguments):n.queue.push(arguments)};\n" +
                " if(!f._fbq)f._fbq=n;n.push=n;n.loaded=!0;n.version='2.0';\n" +
                " n.queue=[];t=b.createElement(e);t.async=!0;\n" +
                " t.src=v;s=b.getElementsByTagName(e)[0];\n" +
                " s.parentNode.insertBefore(t,s)}(window, document,'script',\n" +
                " 'https://connect.facebook.net/en_US/fbevents.js');\n" +
                " fbq('init', '306357120857848');\n" +
                " fbq('track', 'PageView');\n" +
                " </script>\n" +
                "   <noscript>\n" +
                "    &lt;img height=\"1\" width=\"1\" style=\"display:none\" src=\"https://www.facebook.com/tr?id=306357120857848&amp;amp;ev=PageView&amp;amp;noscript=1\"&gt;\n" +
                "   </noscript><!-- End Facebook Pixel Code --> <!-- VK Pixel Code -->\n" +
                "   <script type=\"text/javascript\">(window.Image ? (new Image()) : document.createElement('img')).src = 'https://vk.com/rtrg?p=VK-RTRG-231549-gyQWa';</script><!-- End VK Pixel Code --> <!-- Счетчик pro.culture.ru -->\n" +
                "   <script src=\"https://culturaltracking.ru/static/js/spxl.js\" data-pixel-id=\"608\"></script><!-- End Счетчик pro.culture.ru -->\n" +
                "   <meta name=\"facebook-domain-verification\" content=\"50z62vflkn2wcwpq3u5cjih6j9epfz\">\n" +
                "  </head>\n" +
                "  <body><!-- Google Tag Manager (noscript) -->\n" +
                "   <noscript>\n" +
                "    <iframe src=\"https://www.googletagmanager.com/ns.html?id=GTM-TK6P9QX\" height=\"0\" width=\"0\" style=\"display:none;visibility:hidden\"></iframe>\n" +
                "   </noscript><!-- End Google Tag Manager (noscript) -->\n" +
                "   <div id=\"layout\">\n" +
                "    <div id=\"header\"><a href=\"/mobile/\" class=\"logo\"></a>\n" +
                "     <div class=\"title\">\n" +
                "      Новости\n" +
                "     </div>\n" +
                "     <div class=\"toggle_sidebar\"></div>\n" +
                "    </div>\n" +
                "    <div class=\"page pageNews\"><!--\n" +
                " Для проверки стилей\n" +
                " -->\n" +
                "     <style>\n" +
                " .pageNews {\n" +
                "     display: block;\n" +
                "     position: relative;\n" +
                " }\n" +
                " .photoGallery {\n" +
                "     padding: 0 0 10px;\n" +
                "     width: 350px;\n" +
                "     margin: 0 auto;\n" +
                " }\n" +
                " .photoGallery li.firstImg {\n" +
                "     float: none;\n" +
                " }\n" +
                " .photoGallery li.firstImg img {\n" +
                "     width: 350px;\n" +
                "     height: 231px;\n" +
                " }\n" +
                " .photoGallery li:not(.firstImg) img {\n" +
                "     width: 81px;\n" +
                "     height: 50px;\n" +
                " }\n" +
                " .photoGallery img {\n" +
                "     border-radius: 5px;\n" +
                " }\n" +
                " h1 {\n" +
                "     font: 16px/1.2em 'Hvn';\n" +
                " \ttext-transform: uppercase;\n" +
                "     margin-bottom: 10px;\n" +
                "     padding: 0 10px;\n" +
                " }\n" +
                " .date {\n" +
                "     color: #777;\n" +
                "     display: block;\n" +
                "     font: 14px 'Hvn Regular';\n" +
                "     padding: 0 10px;\n" +
                "     margin-bottom: 10px;\n" +
                " }\n" +
                " .details {\n" +
                "     padding: 0 10px;\n" +
                "     margin-bottom: 10px;\n" +
                " \tcolor: #252525;\n" +
                "     font: 14px/1.2em 'Hvn Regular';\n" +
                " }\n" +
                " \n" +
                " /*\n" +
                "   Страница Новости\n" +
                " */\n" +
                " .pageNews a {\n" +
                "     color: #990101;\n" +
                " }\n" +
                " .pageNews a:hover {\n" +
                "     color: #ce0402\n" +
                " }\n" +
                " </style>\n" +
                "     <ul class=\"photoGallery\">\n" +
                "      <li class=\"firstImg\"><a href=\"/upload/iblock/325/325e6dfea2199101ad626cdfd1c3123c.jpg\" data-fancybox-group=\"gallery\"><img src=\"/upload/resize_cache/iblock/325/350_231_2/325e6dfea2199101ad626cdfd1c3123c.jpg\" alt=\"С Днем Рождения поздравляем актера &quot;Et Cetera&quot; Данила Никитина\" data-id=\"24697\" data-order=\"2147483647\"></a></li>\n" +
                "     </ul>\n" +
                "     <div class=\"clearFix\"></div>\n" +
                "     <h1 class=\"header\">С Днем Рождения поздравляем актера \"Et Cetera\" Данила Никитина</h1>\n" +
                "     <div class=\"clearFix\"></div><span class=\"date\">06.08.2023</span>\n" +
                "     <div class=\"details\">\n" +
                "      Сегодня День Рождения отмечает актер Данил Никитин<br><br>\n" +
                "       Дорогой Данил, поздравляем с Днем Рождения! Желаем счастья, любви, ярких творческих проектов и интересных ролей! Пусть каждый выход на сцену приносит только радость!\n" +
                "     </div><!--\n" +
                " Array\n" +
                " (\n" +
                "     [youtube] => Array\n" +
                "         (\n" +
                "             [ID] => 73\n" +
                "             [TIMESTAMP_X] => 2020-03-19 16:47:17\n" +
                "             [IBLOCK_ID] => 1\n" +
                "             [NAME] => код ролика youtube\n" +
                "             [ACTIVE] => Y\n" +
                "             [SORT] => 400\n" +
                "             [CODE] => youtube\n" +
                "             [DEFAULT_VALUE] => \n" +
                "             [PROPERTY_TYPE] => S\n" +
                "             [ROW_COUNT] => 1\n" +
                "             [COL_COUNT] => 30\n" +
                "             [LIST_TYPE] => L\n" +
                "             [MULTIPLE] => Y\n" +
                "             [XML_ID] => \n" +
                "             [FILE_TYPE] => \n" +
                "             [MULTIPLE_CNT] => 5\n" +
                "             [TMP_ID] => \n" +
                "             [LINK_IBLOCK_ID] => 0\n" +
                "             [WITH_DESCRIPTION] => N\n" +
                "             [SEARCHABLE] => N\n" +
                "             [FILTRABLE] => N\n" +
                "             [IS_REQUIRED] => N\n" +
                "             [VERSION] => 1\n" +
                "             [USER_TYPE] => \n" +
                "             [USER_TYPE_SETTINGS] => \n" +
                "             [HINT] => \n" +
                "             [PROPERTY_VALUE_ID] => \n" +
                "             [VALUE] => \n" +
                "             [DESCRIPTION] => \n" +
                "             [VALUE_ENUM] => \n" +
                "             [VALUE_XML_ID] => \n" +
                "             [VALUE_SORT] => \n" +
                "             [~VALUE] => \n" +
                "             [~DESCRIPTION] => \n" +
                "             [~NAME] => код ролика youtube\n" +
                "             [~DEFAULT_VALUE] => \n" +
                "         )\n" +
                " \n" +
                "     [photos] => Array\n" +
                "         (\n" +
                "             [ID] => 24\n" +
                "             [TIMESTAMP_X] => 2014-11-13 02:10:56\n" +
                "             [IBLOCK_ID] => 1\n" +
                "             [NAME] => Дополнительные фото\n" +
                "             [ACTIVE] => Y\n" +
                "             [SORT] => 500\n" +
                "             [CODE] => photos\n" +
                "             [DEFAULT_VALUE] => \n" +
                "             [PROPERTY_TYPE] => F\n" +
                "             [ROW_COUNT] => 1\n" +
                "             [COL_COUNT] => 30\n" +
                "             [LIST_TYPE] => L\n" +
                "             [MULTIPLE] => Y\n" +
                "             [XML_ID] => \n" +
                "             [FILE_TYPE] => \n" +
                "             [MULTIPLE_CNT] => 5\n" +
                "             [TMP_ID] => \n" +
                "             [LINK_IBLOCK_ID] => 0\n" +
                "             [WITH_DESCRIPTION] => Y\n" +
                "             [SEARCHABLE] => N\n" +
                "             [FILTRABLE] => N\n" +
                "             [IS_REQUIRED] => N\n" +
                "             [VERSION] => 1\n" +
                "             [USER_TYPE] => \n" +
                "             [USER_TYPE_SETTINGS] => \n" +
                "             [HINT] => \n" +
                "             [PROPERTY_VALUE_ID] => Array\n" +
                "                 (\n" +
                "                     [0] => 110241\n" +
                "                 )\n" +
                " \n" +
                "             [VALUE] => Array\n" +
                "                 (\n" +
                "                     [0] => 24697\n" +
                "                 )\n" +
                " \n" +
                "             [DESCRIPTION] => Array\n" +
                "                 (\n" +
                "                     [0] => \n" +
                "                 )\n" +
                " \n" +
                "             [VALUE_ENUM] => \n" +
                "             [VALUE_XML_ID] => \n" +
                "             [VALUE_SORT] => \n" +
                "             [~VALUE] => Array\n" +
                "                 (\n" +
                "                     [0] => 24697\n" +
                "                 )\n" +
                " \n" +
                "             [~DESCRIPTION] => Array\n" +
                "                 (\n" +
                "                     [0] => \n" +
                "                 )\n" +
                " \n" +
                "             [~NAME] => Дополнительные фото\n" +
                "             [~DEFAULT_VALUE] => \n" +
                "         )\n" +
                " \n" +
                "     [IMG_ORDER] => Array\n" +
                "         (\n" +
                "             [ID] => 188\n" +
                "             [TIMESTAMP_X] => 2020-01-10 23:53:15\n" +
                "             [IBLOCK_ID] => 1\n" +
                "             [NAME] => IMG_ORDER\n" +
                "             [ACTIVE] => Y\n" +
                "             [SORT] => 600\n" +
                "             [CODE] => IMG_ORDER\n" +
                "             [DEFAULT_VALUE] => \n" +
                "             [PROPERTY_TYPE] => S\n" +
                "             [ROW_COUNT] => 1\n" +
                "             [COL_COUNT] => 30\n" +
                "             [LIST_TYPE] => L\n" +
                "             [MULTIPLE] => N\n" +
                "             [XML_ID] => \n" +
                "             [FILE_TYPE] => \n" +
                "             [MULTIPLE_CNT] => 5\n" +
                "             [TMP_ID] => \n" +
                "             [LINK_IBLOCK_ID] => 0\n" +
                "             [WITH_DESCRIPTION] => N\n" +
                "             [SEARCHABLE] => N\n" +
                "             [FILTRABLE] => N\n" +
                "             [IS_REQUIRED] => N\n" +
                "             [VERSION] => 1\n" +
                "             [USER_TYPE] => \n" +
                "             [USER_TYPE_SETTINGS] => \n" +
                "             [HINT] => \n" +
                "             [PROPERTY_VALUE_ID] => \n" +
                "             [VALUE] => \n" +
                "             [DESCRIPTION] => \n" +
                "             [VALUE_ENUM] => \n" +
                "             [VALUE_XML_ID] => \n" +
                "             [VALUE_SORT] => \n" +
                "             [~VALUE] => \n" +
                "             [~DESCRIPTION] => \n" +
                "             [~NAME] => IMG_ORDER\n" +
                "             [~DEFAULT_VALUE] => \n" +
                "         )\n" +
                " \n" +
                "     [DESCRIPTION] => \n" +
                " )\n" +
                " \n" +
                " -->\n" +
                "     <style>\n" +
                " iframe, object, embed {\n" +
                "         max-width: 100%;\n" +
                "         max-height: 100%;\n" +
                " }\n" +
                " </style>\n" +
                "     <div class=\"clearFix\"></div>\n" +
                "    </div>\n" +
                "    <div id=\"footer\">\n" +
                "     <div class=\"links\"><a href=\"http://et-cetera.ru/?mobile=no\" class=\"desctop\">Полная версия сайта</a>\n" +
                "     </div>\n" +
                "     <div class=\"copyright\">\n" +
                "      2023 © Et Cetera\n" +
                "     </div>\n" +
                "    </div>\n" +
                "   </div>\n" +
                "   <div id=\"sidebar_overlay\"></div>\n" +
                "   <div id=\"sidebar\"><a href=\"//iframeab-pre0932.intickets.ru/\" class=\"head\"> <img src=\"/mobile/_img/tickets3.png\" alt=\"\"> <span class=\"username\">Купить билет</span> </a>\n" +
                "    <div class=\"menu\"><a href=\"/mobile/poster/\" class=\"hubs\">Афиша</a> <a href=\"/mobile/news/\" class=\"hubs\">Новости</a> <a href=\"/mobile/performance/\" class=\"hubs\">Спектакли</a> <a href=\"/mobile/contacts/\" class=\"companies\">Контакты</a> <a href=\"/mobile/to/\" class=\"hubs\">ТО</a> <!--<a href=\"https://www.facebook.com/TheatreEtCetera/\" target=\"_blank\" style=\"background-image: url(/_images/iconFb.png); background-position: 16px; background-repeat: no-repeat;\"></a>--> <a href=\"http://vkontakte.ru/club20489512\" target=\"_blank\" style=\"background-image: url(/_images/iconVk.png); background-position: 16px; background-repeat: no-repeat;\"></a> <a href=\"http://www.youtube.com/user/TheatreEtCetera\" target=\"_blank\" style=\"background-image: url(/_images/iconYt.png); background-position: 16px; background-repeat: no-repeat;\"></a> <a href=\"https://t.me/theatre_etcetera\" target=\"_blank\" style=\"background-image: url(/_images/iconTg.png); background-position: 16px; background-repeat: no-repeat;\"></a> <!--<a href=\"http://instagram.com/theatre_etcetera\" target=\"_blank\" style=\"background-image: url(/_images/iconHz.png); background-position: 16px; background-repeat: no-repeat;\"></a>-->\n" +
                "    </div>\n" +
                "   </div><!-- Yandex.Metrika counter -->\n" +
                "   <script type=\"text/javascript\">var yaParams = {/*Здесь параметры визита*/};</script>\n" +
                "   <script type=\"text/javascript\">(function (d, w, c) { (w[c] = w[c] || []).push(function() { try { w.yaCounter26781675 = new Ya.Metrika({id:26781675, webvisor:true, clickmap:true, trackLinks:true, accurateTrackBounce:true,params:window.yaParams||{ }}); } catch(e) { } }); var n = d.getElementsByTagName(\"script\")[0], s = d.createElement(\"script\"), f = function () { n.parentNode.insertBefore(s, n); }; s.type = \"text/javascript\"; s.async = true; s.src = (d.location.protocol == \"https:\" ? \"https:\" : \"http:\") + \"//mc.yandex.ru/metrika/watch.js\"; if (w.opera == \"[object Opera]\") { d.addEventListener(\"DOMContentLoaded\", f, false); } else { f(); } })(document, window, \"yandex_metrika_callbacks\");</script>\n" +
                "   <noscript>\n" +
                "    <div>\n" +
                "     <img src=\"//mc.yandex.ru/watch/26781675\" style=\"position:absolute; left:-9999px;\" alt=\"\">\n" +
                "    </div>\n" +
                "   </noscript><!-- /Yandex.Metrika counter --> <!-- Екатерина -->\n" +
                "   <script type=\"text/javascript\">!function(){var t=document.createElement(\"script\");t.type=\"text/javascript\",t.async=!0,t.src=\"https://vk.com/js/api/openapi.js?169\",t.onload=function(){VK.Retargeting.Init(\"VK-RTRG-926297-hXPUb\"),VK.Retargeting.Hit()},document.head.appendChild(t)}();</script>\n" +
                "   <noscript>\n" +
                "    <img src=\"https://vk.com/rtrg?p=VK-RTRG-926297-hXPUb\" style=\"position:fixed; left:-999px;\" alt=\"\">\n" +
                "   </noscript><!-- Facebook Pixel Code -->\n" +
                "   <script>\n" +
                " !function(f,b,e,v,n,t,s){if(f.fbq)return;n=f.fbq=function(){n.callMethod?n.callMethod.apply(n,arguments):n.queue.push(arguments)};if(!f._fbq)f._fbq=n;n.push=n;n.loaded=!0;n.version='2.0';n.queue=[];t=b.createElement(e);t.async=!0;t.src=v;s=b.getElementsByTagName(e)[0];s.parentNode.insertBefore(t,s)}(window,document,'script','https://connect.facebook.net/en_US/fbevents.js');\n" +
                " fbq('init', '166074288854711');\n" +
                " fbq('track', 'PageView');\n" +
                " fbq('track', 'AddPaymentInfo');\n" +
                " \t//fbq('track', 'AddToCart');\n" +
                " \t//fbq('track', 'AddToWishlist');\n" +
                " \t//fbq('track', 'CustomizeProduct');\n" +
                " fbq('track', 'InitiateCheckout');\n" +
                " fbq('track', 'Purchase');\n" +
                " \n" +
                " </script>\n" +
                "   <noscript><img height=\"1\" width=\"1\" src=\"https://www.facebook.com/tr?id=166074288854711&amp;ev=PageView&amp;noscript=1\">\n" +
                "   </noscript><!-- End Facebook Pixel Code --> <!-- SendPulse \n" +
                " <script src=\"//static-login.sendpulse.com/apps/fc3/build/loader.js\" sp-form-id=\"d6a2e9909af55e119fafab5d1a9b44e2a2314d2e74c2ce76188496c484a77f55\"></script>\n" +
                "  /SendPulse -->\n" +
                "  </body>\n" +
                " </html>";
    public static void main(String[] args) {
        System.out.println(title);
        parseDocument(content);
    }

    public static void parseDocument(String doc) {
        Document document = Jsoup.parse(doc);
        Elements title = document.getElementsByTag("title").remove();
        Elements description = document.getElementsByTag("description").remove();
        Elements footer = document.getElementsByTag("footer").remove();
        Elements h1Elements = document.getElementsByTag("h1").remove();
        Elements h2Elements = document.getElementsByTag("H2").remove();
        Elements body = document.getElementsByTag("body").remove();
        System.out.println(title.text() + "\n"
                + description.text() + "\n"
                + footer.text() + "\n"
                + h1Elements.text() + "\n"
                + h2Elements.text() + "\n"
                + body.text());
    }
}
