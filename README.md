#Disk Cache Manager for Android

##Dependency

+ [DiskLruCache][1]
+ [Gson][2]

##Usage

```

// init
CacheManager cacheManager = CacheManager.load(context);

// put object
Foo foo = new Foo();
cacheManager.put("foo_key", foo, null);
// put collection
List<String> strings = new ArrayList<String>();
strings.add("one");
strings.add("two");
strings.add("three");
Type type = new TypeToken<List<String>>(){}.getType();
cacheManager.put("list_key", strings, type);
        
// get
Foo foo = cacheManager.get("foo_key", Foo.class);
List<String> list = cacheManager.get("list_key", type);
        
// clear cache
cacheManager.clearCache();
```

Note: the put/get/clear operation should **NOT** be done on UI/Main Thread.


[1]: https://github.com/JakeWharton/DiskLruCache
[2]: https://code.google.com/p/google-gson
