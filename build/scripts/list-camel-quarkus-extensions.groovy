/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.util.stream.Collectors

/* Keep in sync with the current file name */
@groovy.transform.Field static final String currentScript = "list-camel-quarkus-extensions.groovy";
final File extensionsPomPath = new File("${project.basedir}/pom.xml")
final File componentExtensionsAdocPath = new File("${project.basedir}/../docs/modules/ROOT/pages/_partials/component-extensions.adoc")
final File commonExtensionsAdocPath = new File("${project.basedir}/../docs/modules/ROOT/pages/_partials/common-extensions.adoc")

def parser = new XmlParser()
def pom = parser.parseText(extensionsPomPath.getText('UTF-8'))
final Set componentExtensions = new TreeSet()
final Set commonExtensions = new TreeSet()
pom.modules.module.each { node ->
    final String key = node.text().trim()
    if (key.startsWith("core") || key.endsWith("-common")) {
        commonExtensions.add(key)
    } else {
        componentExtensions.add(key)
    }
}

void writeFile(File f, Set<String> keys) {
    f.setText('// Generated by ' + currentScript +'\n' + keys.stream().map { k -> '* `camel-quarkus-' + k +'`\n' }.collect(Collectors.joining()), 'UTF-8')
}

writeFile(componentExtensionsAdocPath, componentExtensions)
writeFile(commonExtensionsAdocPath, commonExtensions)
