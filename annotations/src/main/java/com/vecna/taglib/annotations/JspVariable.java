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

package com.vecna.taglib.annotations;

/**
 * JSP tag variable
 * @author ogolberg@vecna.com
 */
public @interface JspVariable {
  /**
   * Given name
   */
  String nameGiven() default "";
  /**
   * Name inferred from an attribute
   */
  String nameFromAttribute() default "";
  /**
   * Variable type
   */
  Class<?> variableClass();
  /**
   * Whether to declare the variable
   */
  boolean declare() default true;
  /**
   * Variable scope
   */
  JspVariableScope scope() default JspVariableScope.NESTED;
}
