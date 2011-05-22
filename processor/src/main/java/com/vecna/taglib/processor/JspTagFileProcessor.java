/**
 * Copyright 2011 Vecna Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
*/

package com.vecna.taglib.processor;

import java.io.File;

import org.apache.commons.lang.StringUtils;

import com.vecna.taglib.model.JspTagFileModel;
import com.vecna.taglib.model.JspTaglibModel;

/**
 * Builds JSP taglib model objects from tag files
 * @author ogolberg@vecna.com
 */
public class JspTagFileProcessor {
  /**
   * Scan a directory with tag files and add info about them to a taglib model
   * @param root webapp root directory
   * @param tagDir tag directory relative to the webapp root
   * @param taglib taglib model object
   */
  public void addLocalMetadata(String root, String tagDir, JspTaglibModel taglib) {
    File dir = new File(root + File.separator + tagDir);
    if (dir.exists() && dir.isDirectory()) {
      String[] files = dir.list();
      for (String file : files) {
        if (file.endsWith(".tag")) {
          JspTagFileModel tagFile = new JspTagFileModel();
          tagFile.name = StringUtils.removeEnd(file, ".tag");
          tagFile.path = tagDir + File.separator + file;
          taglib.tagFiles.add(tagFile);
        }
      }
    }
  }
}
