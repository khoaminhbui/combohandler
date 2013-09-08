package com.khoaminhbui.controller;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.LineIterator;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class ComboHandler {
   private String YUIPath = null;
   private String YUIResourcePath = null;
   private final String YUIRelativePath = "/library/yui/3.6.0/build/";

   private final String excludedRelativeUrlRegex = "data:";
   private final String relativeUrlRegex = "url\\(\\s*(?!/|(?:http))([^\\)]+)\\s*\\)";
   private Pattern relativeUrlPattern = Pattern.compile(relativeUrlRegex);
   private Pattern excludedRelativeUrlPattern = Pattern.compile(excludedRelativeUrlRegex);

   @RequestMapping(value = "/yui", method = RequestMethod.GET)
   public @ResponseBody
   void getYUIResources(HttpServletRequest request, HttpServletResponse response) throws IOException {
      if (YUIPath == null) {
         YUIPath = request.getSession().getServletContext().getRealPath("") + YUIRelativePath;
         YUIResourcePath = request.getContextPath() + YUIRelativePath;
      }

      String ext = null;
      boolean css = false;

      String[] fileNames = request.getQueryString().split("&");
      ServletOutputStream out = response.getOutputStream();
      for (String fileName : fileNames) {
         if (ext == null) {
            ext = FilenameUtils.getExtension(fileName);
            css = "css".equalsIgnoreCase(ext);

            // content type must be set before any writing to response.
            if (css) {
               response.setContentType("text/css");
            }
            else {
               response.setContentType("application/javascript");
            }
         }

         File srcFile = new File(YUIPath, fileName);

         if (css) {
            LineIterator it = FileUtils.lineIterator(srcFile, "UTF-8");
            String line;
            while (it.hasNext()) {
               line = handleRelativeUrl(YUIResourcePath, fileName, it.nextLine()) + System.getProperty("line.separator");
               out.write(line.getBytes());
            }
         }
         else {
            out.write(FileUtils.readFileToByteArray(srcFile));
            out.write(System.getProperty("line.separator").getBytes());
         }
      }

      response.setStatus(HttpServletResponse.SC_OK);
   }

   private Object handleRelativeUrl(String web3rdPartyYUIUrl, String fileName, String line) {
      Matcher relativeUrlMatcher = this.relativeUrlPattern.matcher(line);
      if (relativeUrlMatcher.find()) {
         StringBuffer s = new StringBuffer(line.length() * 2);
         String basePath = FilenameUtils.getPath(fileName);
         if (basePath.startsWith(File.separator)) {
            basePath = basePath.substring(1);
         }

         do {
            String relativeUrl = relativeUrlMatcher.group(1);
            Matcher excludedRelativeUrlMatcher = this.excludedRelativeUrlPattern.matcher(relativeUrl);
            if (!excludedRelativeUrlMatcher.find()) {
               relativeUrlMatcher.appendReplacement(s, "url(" + web3rdPartyYUIUrl + FilenameUtils.separatorsToUnix(FilenameUtils.concat(basePath, relativeUrl)) + ")");
            }
         }
         while (relativeUrlMatcher.find());
         relativeUrlMatcher.appendTail(s);

         return s;
      }

      return line;
   }
}