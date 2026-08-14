import org.springframework.cache.Cache;
public class scratch {
    public static void main(String[] args) {
        try {
            java.lang.reflect.Method m = Cache.class.getMethod("evictIfPresent", Object.class);
            System.out.println("evictIfPresent exists!");
        } catch (Exception e) {
            System.out.println("No evictIfPresent");
        }
    }
}
