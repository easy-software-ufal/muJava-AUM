/**
 * Copyright (C) 2015  the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package mujava.util;

import java.io.File;
import java.io.FilenameFilter;

/**
 * <p>Description: </p>
 * @author Yu-Seung Ma
 * @version 1.0
 */

public class MutantDirFilter implements FilenameFilter {

  public MutantDirFilter() {
  }

  public boolean accept(File dir, String name) {
	if ((name.indexOf("_") == 3) || (name.indexOf("_") == 4)) {
	  return true;
	} else return false;
  }
}
