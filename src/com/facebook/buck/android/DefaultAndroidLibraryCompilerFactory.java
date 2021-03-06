/*
 * Copyright 2016-present Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.facebook.buck.android;

import com.facebook.buck.jvm.kotlin.KotlinBuckConfig;
import com.facebook.buck.jvm.scala.ScalaBuckConfig;
import com.facebook.buck.util.HumanReadableException;

public class DefaultAndroidLibraryCompilerFactory implements AndroidLibraryCompilerFactory {

  private final ScalaBuckConfig scalaConfig;
  private final KotlinBuckConfig kotlinBuckConfig;

  public DefaultAndroidLibraryCompilerFactory(
      ScalaBuckConfig scalaConfig, KotlinBuckConfig kotlinBuckConfig) {
    this.scalaConfig = scalaConfig;
    this.kotlinBuckConfig = kotlinBuckConfig;
  }

  @Override
  public AndroidLibraryCompiler getCompiler(AndroidLibraryDescription.JvmLanguage language) {
    switch (language) {
      case JAVA: return new JavaAndroidLibraryCompiler();
      case SCALA: return new ScalaAndroidLibraryCompiler(scalaConfig);
      case KOTLIN: return new KotlinAndroidLibraryCompiler(kotlinBuckConfig);
    }
    throw new HumanReadableException("Unsupported `language` parameter value: %s", language);
  }
}
