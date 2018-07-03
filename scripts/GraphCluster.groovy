#!/usr/bin/env groovy
/*
 * Copyright (c) 2018 Deutsche Nationalbibliothek
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Groovy: Version 2.5.0
 */
@GrabResolver(name='jitpack', root='https://jitpack.io')
@Grab('com.github.eberhardtj:culturegraph-clustering:v0.2.0')

import groovy.cli.picocli.CliBuilder
import org.culturegraph.clustering.algorithm.core.Algorithm
import org.culturegraph.clustering.algorithm.plugin.ClusterAlgorithm

def summary = '\n' + 'Partitions a bipartite-graph into connected components.\n' +
        'Version: 0.2.0\n'

def cli = new CliBuilder(usage:'GraphCluster [-chs] -i FILE -o FILE', header: '\nOptions:', footer: summary)
cli.with {
    i argName: 'file', longOpt: 'input', 'Input adjacency list (regular or GZIP compressed).', type: String.class, defaultValue: '-', required: true
    o argName: 'file', longOpt: 'output', 'Output node to cluster mapper.', type: String.class, defaultValue: '-', required: true
    s argName: 'num', longOpt: 'size', 'Minimum component size (Default: 0).', type: Integer.class, defaultValue: 0
    _ argName: 'param', longOpt: 'loglevel', 'Log level (off, info, debug).', type: String.class, defaultValue: 'info'
    c longOpt: 'compress', 'Compress output.'
    h longOpt: 'help', 'Show usage information.'
}

def options = cli.parse(args)

if (!options) {
    return
}

if (options.h) {
    cli.usage()
    return
}

File inputAdjacencyList = new File( (String)options.i )
File outputMapping = new File( (String)options.o )
boolean compressOutput = (Boolean)options.c
int minimumComponentSize = options.s ? (Integer)options.s : 0

String loglevel = options.loglevel ? options.loglevel : 'info'
System.setProperty('org.slf4j.simpleLogger.defaultLogLevel', loglevel)

Algorithm cluster = new ClusterAlgorithm(inputAdjacencyList, outputMapping, compressOutput, minimumComponentSize)
cluster.run()