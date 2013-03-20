/**
 * Copyright (C) 2013 HalZhang
 */

package com.halzhang.android.apps.startupnews.parser;

import com.halzhang.android.apps.startupnews.entity.SNComment;
import com.halzhang.android.apps.startupnews.entity.SNDiscuss;
import com.halzhang.android.apps.startupnews.entity.SNNew;
import com.halzhang.android.apps.startupnews.entity.SNUser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * StartupNews
 * <p>
 * </p>
 * 
 * @author <a href="http://weibo.com/halzhang">Hal</a>
 * @version Mar 19, 2013
 */
public class SNDiscussParser extends BaseHTMLParser<SNDiscuss> {

    @Override
    public SNDiscuss parseDocument(Document doc) throws Exception {
        SNDiscuss discuss = new SNDiscuss();
        if (doc == null) {
            return discuss;
        }
        // news
        Elements tableRows = doc.select("table tr table");
        if (tableRows != null && tableRows.size() > 1) {
            String voteURL = null;
            String title = null;
            String url = null;
            String urlDomain = null;
            String subText = null;
            SNUser user = null;
            int points = 0;
            int commentsCount = 0;
            String postID = null;
            String discussURL = null;
            Element newsTableElement = tableRows.get(1);
            Elements trElements = newsTableElement.getElementsByTag("tr");
            Element titleTrElement = trElements.get(0);
            Element voteElement = titleTrElement.select("tr > td:eq(0) a").first();
            if (voteElement != null) {
                voteURL = resolveRelativeSNURL(voteElement.attr("href"));
            }
            Element titleAElement = titleTrElement.select("tr > td:eq(1) a").first();
            if (titleAElement != null) {
                url = titleAElement.attr("href");
                urlDomain = getDomainName(url);
                title = titleAElement.text();
            }

            Element subTextTdeElement = trElements.get(1).select("td.subtext").first();
            subText = subTextTdeElement.html();
            points = getIntValueFollowedBySuffix(subTextTdeElement.select("td > span").text(), " p");

            String author = subTextTdeElement.select("td > a[href*=user]").text();
            user = new SNUser();
            user.setName(author);
            user.setId(author);
            Element e2 = subTextTdeElement.select("td > a[href*=item]").first();
            if (e2 != null) {
                commentsCount = getIntValueFollowedBySuffix(e2.text(), " c");
                if (commentsCount == BaseHTMLParser.UNDEFINED && e2.text().contains("discuss"))
                    commentsCount = 0;
                postID = getStringValuePrefixedByPrefix(e2.attr("href"), "id=");
                discussURL = resolveRelativeSNURL(e2.attr("href"));
            } else {
                commentsCount = BaseHTMLParser.UNDEFINED;
            }

            discuss.setSnNew(new SNNew(url, title, urlDomain, voteURL, points, commentsCount,
                    subText, discussURL, user, postID));

            String fnid = trElements.get(3).getElementsByTag("input").first().attr("value");
            discuss.setFnid(fnid);

        }
        tableRows = doc.select("table tr table tr table tr");
        if (tableRows != null && tableRows.size() > 0) {
            Element rowElement = null;
            SNComment comment = null;
            for (int row = 0; row < tableRows.size(); row++) {
                comment = new SNComment();
                rowElement = tableRows.get(row);
                Element voteAElement = rowElement.select("tr > td:eq(1) a").first();
                if (voteAElement != null) {
                    comment.setVoteURL(resolveRelativeSNURL(voteAElement.attr("href")));
                }
                Elements aElements = rowElement.select("tr > td:eq(2) a");
                SNUser user = new SNUser();
                user.setId(aElements.first().text());
                comment.setUser(user);
                comment.setLink(resolveRelativeSNURL(aElements.last().attr("href")));
                comment.setText(rowElement.select("tr > td:eq(2) > span").first().text());
                comment.setReplayURL(resolveRelativeSNURL(rowElement.select("tr > td:eq(2) > p a")
                        .first().attr("href")));
                discuss.getComments().add(comment);
            }
        }
        return discuss;
    }

}
