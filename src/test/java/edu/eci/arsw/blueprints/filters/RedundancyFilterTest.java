package edu.eci.arsw.blueprints.filters;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RedundancyFilterTest {

    private final RedundancyFilter filter = new RedundancyFilter();

    @Test
    void removesConsecutiveDuplicatePoints() {
        Blueprint bp = new Blueprint("john", "house", List.of(
                new Point(1, 1), new Point(1, 1), new Point(2, 2), new Point(2, 2), new Point(1, 1)));

        Blueprint result = filter.apply(bp);

        assertThat(result.getPoints()).containsExactly(
                new Point(1, 1), new Point(2, 2), new Point(1, 1));
    }

    @Test
    void keepsPointsWhenNoDuplicates() {
        Blueprint bp = new Blueprint("john", "house", List.of(new Point(0, 0), new Point(1, 1)));

        Blueprint result = filter.apply(bp);

        assertThat(result.getPoints()).containsExactly(new Point(0, 0), new Point(1, 1));
    }

    @Test
    void handlesEmptyPointList() {
        Blueprint bp = new Blueprint("john", "house", List.of());

        Blueprint result = filter.apply(bp);

        assertThat(result.getPoints()).isEmpty();
    }
}
