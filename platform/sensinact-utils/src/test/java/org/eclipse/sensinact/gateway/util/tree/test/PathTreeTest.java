/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.util.tree.test;

import org.eclipse.sensinact.gateway.util.tree.PathNode;
import org.eclipse.sensinact.gateway.util.tree.PathNodeFactory;
import org.eclipse.sensinact.gateway.util.tree.PathTree;
import org.junit.jupiter.api.Test;


/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class PathTreeTest {

    class MyTree<P extends PathNode<P>> extends PathTree<P> {

        MyTree() {
            super(new PathNodeFactory<P>() {
                @Override
                public P createPathNode(String nodeName) {
                    return (P) new PathNode<P>(nodeName);
                }

                @Override
                public P createPatternNode(String nodeName) {
                    return (P) new PathNode<P>(nodeName, true);
                }

                @Override
                public P createRootNode() {
                    return (P) new PathNode<P>();
                }
            });
        }
    }


    /**
     *
     */
    @Test
    public <P extends PathNode<P>> void testTree() {
        MyTree<P> tree = new MyTree<P>();
        tree.add("/aaaaaaa");
        tree.add("/vvvvvvv");
        tree.add("/ccccccc");
        tree.add("/uuuuuuu");
        tree.add("/hhhhhhh");

        tree.add("/test/service/resource");
        tree.add("/test/", ".*", true);
        tree.add("/test/.*/location");

        System.out.println(tree.toString());

        P node = tree.get("/test/myservice/location");
        System.out.println(node.getPath());
        tree.delete("/test/service");
        System.out.println(tree.toString());

        tree.delete("/xxxxx");
        System.out.println(tree.toString());


    }
}
