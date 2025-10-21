package ru.sentidas.rangiffler.test.gql.support;

import ru.sentidas.GetFeedQuery;

import java.util.HashMap;
import java.util.Map;

public class FeedUtil {

    public static Map<String, Integer> countyCountFromStat(GetFeedQuery.Data query) {
        Map<String, Integer> counts = new HashMap<>();
        for (GetFeedQuery.Stat stat : query.feed.stat) {
            String countryCode = stat.country.code;
            Integer count = stat.count;
            counts.put(countryCode, count);
        }
        return counts;
    }
}
