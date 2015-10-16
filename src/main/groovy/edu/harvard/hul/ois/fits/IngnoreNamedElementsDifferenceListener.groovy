/*
 * Copyright 2015 Harvard University Library
 *
 * This file is part of FITS (File Information Tool Set).
 *
 * FITS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FITS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FITS.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.harvard.hul.ois.fits

import java.util.HashSet
import java.util.Set

import org.custommonkey.xmlunit.Difference
import org.custommonkey.xmlunit.DifferenceConstants
import org.custommonkey.xmlunit.DifferenceListener
import org.w3c.dom.Node;

public class IgnoreNamedElementsDifferenceListener implements DifferenceListener {
    private Set<String> blackList = new HashSet<String>()

    public IgnoreNamedElementsDifferenceListener(String ...elementNames) {
        for (String name : elementNames) {
            blackList.add(name)
        }
    }

    public int differenceFound(Difference difference) {
    	// If Element or attribute has name in the blackList, ignore differences
        if (difference.getId() == DifferenceConstants.TEXT_VALUE_ID) {
			// DEBUG
			// println difference.getControlNodeDetail().getNode().getParentNode().getNodeName()
            if (blackList.contains(difference.getControlNodeDetail().getNode().getParentNode().getNodeName())) {
                return DifferenceListener.RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL
            }
        }
        else if (difference.getId() == DifferenceConstants.ATTR_VALUE_ID) {
			// DEBUG
			// println difference.getControlNodeDetail().getNode().getNodeName()
            if (blackList.contains(difference.getControlNodeDetail().getNode().getNodeName())) {
                return DifferenceListener.RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL
            }
        }

        return DifferenceListener.RETURN_ACCEPT_DIFFERENCE;
    }

    public void skippedComparison(Node node, Node node1) {

    }
}