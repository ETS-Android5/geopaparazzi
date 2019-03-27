package org.locationtech.jts.operation.distance;

import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryComponentFilter;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.index.strtree.STRtree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FacetSequenceTreeBuilder {
  // 6 seems to be a good facet sequence size
  private static final int FACET_SEQUENCE_SIZE = 6;

  // Seems to be better to use a minimum node capacity
  private static final int STR_TREE_NODE_CAPACITY = 4;

  public static STRtree build(Geometry g) {
    STRtree tree = new STRtree(STR_TREE_NODE_CAPACITY);
    List sections = computeFacetSequences(g);
    for (Iterator i = sections.iterator(); i.hasNext();) {
      FacetSequence section = (FacetSequence) i.next();
      tree.insert(section.getEnvelope(), section);
    }
    tree.build();
    return tree;
  }

  /**
   * Creates facet sequences
   * 
   * @param g
   * @return List<GeometryFacetSequence>
   */
  private static List computeFacetSequences(Geometry g) {
    final List sections = new ArrayList();

    g.apply(new GeometryComponentFilter() {

      public void filter(Geometry geom) {
        CoordinateSequence seq = null;
        if (geom instanceof LineString) {
          seq = ((LineString) geom).getCoordinateSequence();
          addFacetSequences(seq, sections);
        }
        else if (geom instanceof Point) {
          seq = ((Point) geom).getCoordinateSequence();
          addFacetSequences(seq, sections);
        }
      }
    });
    return sections;
  }

  private static void addFacetSequences(CoordinateSequence pts, List sections) {
    int i = 0;
    int size = pts.size();
    while (i <= size - 1) {
      int end = i + FACET_SEQUENCE_SIZE + 1;
      // if only one point remains after this section, include it in this
      // section
      if (end >= size - 1)
        end = size;
      FacetSequence sect = new FacetSequence(pts, i, end);
      sections.add(sect);
      i = i + FACET_SEQUENCE_SIZE;
    }
  }
}