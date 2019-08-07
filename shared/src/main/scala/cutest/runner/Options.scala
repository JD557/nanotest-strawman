/*
 * Copyright (c) 2014-2019 by The Minitest Project Developers.
 * Some rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cutest.runner

/**
 * Configurable options for the runner.
 *
 * @param useSbtLogging specifies whether to use SBT's test-logging infrastructure,
 *        or just println.
 *
 *        Defaults to `println` because SBT's test logging doesn't seem to give us
 *        anything that we want, and does annoying things like making a left-hand
 *        gutter and buffering input by default.
 *
 *        Option inspired by its availability in uTest and other testing frameworks.
 */
final case class Options(
    useSbtLogging: Boolean = false
)
