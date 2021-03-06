/*
 * Copyright 2015-present Facebook, Inc.
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

package com.facebook.buck.go;

import com.facebook.buck.model.BuildTarget;
import com.facebook.buck.model.Flavor;
import com.facebook.buck.model.Flavored;
import com.facebook.buck.model.HasTests;
import com.facebook.buck.parser.NoSuchBuildTargetException;
import com.facebook.buck.rules.AbstractDescriptionArg;
import com.facebook.buck.rules.BuildRule;
import com.facebook.buck.rules.BuildRuleParams;
import com.facebook.buck.rules.BuildRuleResolver;
import com.facebook.buck.rules.BuildTargetSourcePath;
import com.facebook.buck.rules.Description;
import com.facebook.buck.rules.Hint;
import com.facebook.buck.rules.NoopBuildRule;
import com.facebook.buck.rules.SourcePath;
import com.facebook.buck.rules.SourcePathResolver;
import com.facebook.buck.rules.TargetGraph;
import com.facebook.infer.annotation.SuppressFieldNotInitialized;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;

import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

public class GoLibraryDescription
    implements
    Description<GoLibraryDescription.Arg>,
    Flavored,
    GoLinkableDescription<GoLibraryDescription.Arg> {

  private final GoBuckConfig goBuckConfig;

  public GoLibraryDescription(GoBuckConfig goBuckConfig) {
    this.goBuckConfig = goBuckConfig;
  }

  @Override
  public Arg createUnpopulatedConstructorArg() {
    return new Arg();
  }

  @Override
  public boolean hasFlavors(ImmutableSet<Flavor> flavors) {
    return goBuckConfig.getPlatformFlavorDomain().containsAnyOf(flavors);
  }

  @Override
  public GoLinkable getLinkable(
      BuildRuleResolver resolver,
      GoLinkableTargetNode<Arg> targetNode,
      GoPlatform platform) {
    BuildTarget target = targetNode.getBuildTarget().withAppendedFlavors(platform.getFlavor());
    SourcePath output;
    try {
      output = new BuildTargetSourcePath(resolver.requireRule(target).getBuildTarget());
    } catch (NoSuchBuildTargetException e) {
      throw new RuntimeException(e);
    }
    Arg args = targetNode.getConstructorArg();
    return GoLinkable.builder()
        .setGoLinkInput(
            ImmutableMap.of(
                args.packageName.map(Paths::get)
                    .orElse(goBuckConfig.getDefaultPackageName(targetNode.getBuildTarget())),
                output))
        .setExportedDeps(args.exportedDeps)
        .build();
  }

  @Override
  public ImmutableSet<GoLinkable> getTransitiveLinkables(
      BuildRuleResolver resolver,
      GoLinkableTargetNode<Arg> targetNode,
      GoPlatform platform) {
    Arg args = targetNode.getConstructorArg();
    return GoDescriptors.requireTransitiveGoLinkables(
        resolver,
        Optional.of(targetNode),
        platform,
        Iterables.concat(
            args.deps,
            args.exportedDeps));
  }

  @Override
  public <A extends Arg> BuildRule createBuildRule(
      TargetGraph targetGraph,
      BuildRuleParams params,
      BuildRuleResolver resolver,
      A args) {
    Optional<GoPlatform> platform =
        goBuckConfig.getPlatformFlavorDomain().getValue(params.getBuildTarget());

    if (platform.isPresent()) {
      return GoDescriptors.createGoCompileRule(
          params,
          resolver,
          goBuckConfig,
          args.packageName.map(Paths::get)
              .orElse(goBuckConfig.getDefaultPackageName(params.getBuildTarget())),
          args.srcs,
          args.compilerFlags,
          args.assemblerFlags,
          platform.get(),
          Iterables.concat(args.deps, args.exportedDeps));
    }

    return new NoopBuildRule(params, new SourcePathResolver(resolver));
  }

  @SuppressFieldNotInitialized
  public static class Arg extends AbstractDescriptionArg implements HasTests {
    public ImmutableSortedSet<SourcePath> srcs = ImmutableSortedSet.of();
    public List<String> compilerFlags = ImmutableList.of();
    public List<String> assemblerFlags = ImmutableList.of();
    public Optional<String> packageName;
    public ImmutableSortedSet<GoLinkableTargetNode<?>> deps = ImmutableSortedSet.of();
    public ImmutableSortedSet<GoLinkableTargetNode<?>> exportedDeps = ImmutableSortedSet.of();

    @Hint(isDep = false) public ImmutableSortedSet<BuildTarget> tests = ImmutableSortedSet.of();

    @Override
    public ImmutableSortedSet<BuildTarget> getTests() {
      return tests;
    }
  }
}
