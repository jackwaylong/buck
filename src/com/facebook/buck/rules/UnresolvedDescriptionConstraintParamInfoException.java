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

package com.facebook.buck.rules;

import com.facebook.buck.model.BuildTarget;

import java.lang.reflect.Type;

public class UnresolvedDescriptionConstraintParamInfoException extends ParamInfoException {

  private final String parameterName;
  private final BuildTarget reference;
  private final Type expected;
  private final Description<?> actual;

  public UnresolvedDescriptionConstraintParamInfoException(
      String parameterName,
      BuildTarget reference,
      Type expected,
      Description<?> actual) {
    super(
        parameterName,
        String.format(
            "Unexpected target type: '%s' was '%s'",
            parameterName,
            reference,
            Description.getBuildRuleType(actual)));
    this.parameterName = parameterName;
    this.reference = reference;
    this.expected = expected;
    this.actual = actual;
  }

  public String getParameterName() {
    return parameterName;
  }

  public BuildTarget getReference() {
    return reference;
  }

  public Type getExpected() {
    return expected;
  }

  public Description<?> getActual() {
    return actual;
  }

}
