package com.luorrak.ouroboros.api;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.view.View;

import com.luorrak.ouroboros.R;
import com.luorrak.ouroboros.thread.CardDialogFragment;
import com.luorrak.ouroboros.thread.ExternalNavigationWarningFragment;
import com.luorrak.ouroboros.thread.InterThreadNavigationWarningFragment;
import com.luorrak.ouroboros.util.InfiniteDbHelper;
import com.luorrak.ouroboros.util.SpoilerSpan;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.util.List;

/**
 * Ouroboros - An 8chan browser
 * Copyright (C) 2015  Luorrak
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

//Adopted from Chanobol/Clover
public class CommentParser {
    private final String LOG_TAG = CommentParser.class.getSimpleName();

    public Spannable parseId(String id){
        SpannableString coloredId = new SpannableString(id);
        coloredId.setSpan(new ForegroundColorSpan(Color.parseColor("#" + id)), 0, coloredId.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return coloredId;
    }

    public Spannable parseCom(String rawCom, String currentBoard, String resto, FragmentManager fragmentManager, InfiniteDbHelper infiniteDbHelper){

        Document doc = Jsoup.parseBodyFragment(rawCom);
        List<Node> comLineArray = doc.body().childNodes();
        CharSequence processedText = new SpannableString("");


        for (Node comLine : comLineArray){
            //legacy stuff to avoid hard crashes
            if (comLine instanceof TextNode){
                //plain text
                processedText = TextUtils.concat(processedText, "\nDEVELOPER REQUEST: This post was made before 8chan switched it's comment format in April. If this post was made more recently than april, file a bug report with the developer of this app\n");
                break;
            } else {
                Element lineElement = (Element) comLine;
                if (lineElement.hasClass("empty")){
                    processedText = TextUtils.concat(processedText, "\n");
                }else if (lineElement.hasClass("quote")){
                    CharSequence greenText = "";
                    for (Node node : comLine.childNodes()){
                        if (node instanceof TextNode){
                            greenText = TextUtils.concat(greenText, ((TextNode) node).text());
                            continue;
                        }

                        Element e = (Element) node;
                        if (node.nodeName().equals("a")){
                            greenText = TextUtils.concat(greenText, parseAnchor(e, currentBoard, resto, fragmentManager, infiniteDbHelper));
                            continue;
                        }
                        greenText = TextUtils.concat(greenText, e.text());
                    }
                    greenText = parseQuote(greenText);
                    greenText = TextUtils.concat(greenText, "\n");
                    processedText = TextUtils.concat(processedText, greenText);
                }else {
                    for (Node node : comLine.childNodes()){
                        CharSequence parsedNode = parseNode(node, currentBoard, resto, fragmentManager, infiniteDbHelper);
                        processedText = TextUtils.concat(processedText, parsedNode);
                    }
                    processedText = TextUtils.concat(processedText, "\n");
                }
            }


        }
        return SpannableStringBuilder.valueOf(processedText);
    }

    private CharSequence parseQuote(CharSequence node){
        //Element greenTextElement = (Element) node;
        SpannableString greenText = new SpannableString(node);
        greenText.setSpan(new ForegroundColorSpan(Color.parseColor("#789922")), 0, greenText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return greenText;
    }

    private CharSequence parseNode(Node node, String currentBoard, String resto, FragmentManager fragmentManager, InfiniteDbHelper infiniteDbHelper) {
        if (node instanceof TextNode){
            return Html.fromHtml(Html.fromHtml(node.toString()).toString());
            //return node.toString();
        }

        switch (node.nodeName()){
            case "p": {
                Element p = (Element) node;
                if (p.hasClass("empty")){
                    return "\n";
                }
                /*
                if (p.hasClass("quote")){
                    Element quote = (Element) node;
                    SpannableString greenText = new SpannableString(quote.text());
                    greenText.setSpan(new ForegroundColorSpan(Color.parseColor("#789922")), 0, greenText.length(), 0);
                    Log.d(LOG_TAG, "Green Text " + quote.toString() + "Node name " + node.nodeName());
                    return greenText;
                }*/
            }
            case "span": {
                Element span = (Element) node;

                String sclass = span.classNames().iterator().next();

                switch (sclass){
                    case "heading":{
                        Element heading = (Element) node;
                        SpannableString redText = new SpannableString(heading.text());
                        redText.setSpan(new ForegroundColorSpan(Color.RED), 0, redText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        redText.setSpan(new StyleSpan(Typeface.BOLD), 0, redText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        return redText;
                    }
                    case "spoiler": {
                        Element spoiler = (Element) node;
                        SpannableString spoilerText = new SpannableString(spoiler.text());
                        SpoilerSpan spoilerSpan = new SpoilerSpan();
                        spoilerText.setSpan(spoilerSpan, 0, spoilerText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        return spoilerText;
                    }
                }
            }
            case "s":{
                Element s = (Element) node;
                SpannableString strikethrough = new SpannableString(s.text());
                strikethrough.setSpan(new StrikethroughSpan(), 0, strikethrough.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                return strikethrough;
            }
            case "em":{
                Element em = (Element) node;
                SpannableString italic = new SpannableString(em.text());
                italic.setSpan(new StyleSpan(Typeface.ITALIC), 0, italic.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                return italic;
            }
            case "strong":{
                Element strong = (Element) node;
                SpannableString bold = new SpannableString(strong.text());
                bold.setSpan(new StyleSpan(Typeface.BOLD), 0, bold.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                return bold;
            }
            case "a":{
                Element a = (Element) node;

                //Links need to be parsed
                return parseAnchor(a, currentBoard, resto, fragmentManager, infiniteDbHelper);
            }
            default: {
                if (node instanceof Element){
                    return ((Element) node).text();
                }
            }
        }
        return node.toString();
    }

    private CharSequence parseAnchor(final Element anchor, final String currentBoard, String resto, final FragmentManager fragmentManager, InfiniteDbHelper infiniteDbHelper){

        final String linkUrl = anchor.attr("href");
        if (linkUrl.contains("http")){
            //normal link
            SpannableString normalLink = new SpannableString(anchor.text());
            ClickableSpan clickableNormalLink = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    ExternalNavigationWarningFragment dialog = ExternalNavigationWarningFragment.newInstance(linkUrl);
                    dialog.show(fragmentManager, "externallink");
                }
            };
            normalLink.setSpan(new ForegroundColorSpan(Color.parseColor("#0645AD")), 0, normalLink.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            normalLink.setSpan(clickableNormalLink, 0, normalLink.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return normalLink;

        } else if (linkUrl.contains("_g")){
            //normal link
            SpannableString normalLink = new SpannableString(anchor.text());
            ClickableSpan clickableNormalLink = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    ExternalNavigationWarningFragment dialog = ExternalNavigationWarningFragment.newInstance(anchor.text());
                    dialog.show(fragmentManager, "externallink");
                }
            };
            normalLink.setSpan(new ForegroundColorSpan(Color.parseColor("#0645AD")), 0, normalLink.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            normalLink.setSpan(clickableNormalLink, 0, normalLink.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return normalLink;
        } else if (linkUrl.contains(resto)){
            //same thread
            String anchorText = infiniteDbHelper.isNoUserPost(currentBoard, linkUrl.split("#")[1]) ? anchor.text() + " (You)" : anchor.text();
            SpannableString sameThread = new SpannableString(anchorText);
            ClickableSpan clickableSameThreadLink = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    CardDialogFragment cardDialogFragment = CardDialogFragment.showPost(linkUrl, currentBoard);
                    fragmentTransaction.add(R.id.placeholder_card_dialog, cardDialogFragment)
                            .addToBackStack("threadDialog")
                            .commit();

                }
            };
            sameThread.setSpan(clickableSameThreadLink, 0, sameThread.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            sameThread.setSpan(new ForegroundColorSpan(Color.parseColor("#FF6600")), 0, sameThread.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return sameThread;

        } else {
            //different thread
            SpannableString differentThread = new SpannableString(anchor.text());
            ClickableSpan clickableDifferentThreadLink = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    InterThreadNavigationWarningFragment dialog = InterThreadNavigationWarningFragment.newInstance(linkUrl);
                    dialog.show(fragmentManager, "internallink");
                }
            };
            differentThread.setSpan(clickableDifferentThreadLink, 0, differentThread.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            differentThread.setSpan(new ForegroundColorSpan(Color.parseColor("#FF6600")), 0, differentThread.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return differentThread;
        }
    }


}

