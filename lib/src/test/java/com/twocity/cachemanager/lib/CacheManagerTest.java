package com.twocity.cachemanager.lib;

import com.google.gson.reflect.TypeToken;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Created by twocity on 14-3-5.
 */

@RunWith(RobolectricGradleTestRunner.class)
public class CacheManagerTest {

    CacheManager mCacheManager;

    @Test
    public void testObject() {
        // set
        Foo foo = new Foo("twocity", "a tale of twocities");
        String key = "foo";
        mCacheManager.put(key, foo, null);

        boolean has = mCacheManager.exists(key);
        assertThat(has).isTrue();

        // get
        Foo foo2 = mCacheManager.get(key, Foo.class);
        assertThat(foo2).isNotNull();
        assertThat(foo2.name).isEqualTo("twocity");
        assertThat(foo2.desc).isEqualTo("a tale of twocities");
        clear();
    }

    @Test
    public void testCollections() {
        String key = "list";
        List<String> list = new ArrayList<String>();
        list.add("one");
        list.add("two");
        list.add("three");
        Type type = new TypeToken<List<String>>() {
        }.getType();
        mCacheManager.put(key, list, type);

        boolean has = mCacheManager.exists(key);

        assertThat(has).isTrue();

        List<String> strings = mCacheManager.get(key, type);
        assertThat(strings).isNotNull();
        assertThat(strings).isNotEmpty();
        assertThat(strings.size()).isEqualTo(3);
        assertThat(strings.get(0)).isEqualTo("one");
        assertThat(strings.get(1)).isEqualTo("two");
        assertThat(strings.get(2)).isEqualTo("three");

        clear();
    }

    private void clear() {
        mCacheManager.clearCache();
    }

    @Before
    public void setUp() {
        mCacheManager = CacheManager.load(Robolectric.application);
        long size = mCacheManager.getDiskLruCacheSize();
        assertThat(size).isEqualTo(10 * 1024 * 1024);
    }
}
