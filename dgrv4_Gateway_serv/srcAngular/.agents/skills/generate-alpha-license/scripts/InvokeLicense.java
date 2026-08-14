import java.lang.reflect.Method;

public class InvokeLicense {
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            throw new IllegalArgumentException("Expected expiry year, month, and day.");
        }

        int year = Integer.parseInt(args[0]);
        int month = Integer.parseInt(args[1]);
        int day = Integer.parseInt(args[2]);

        Class<?> utilClass = Class.forName("tpi.dgrv4.enterprise.LicenseUtil");
        Class<? extends Enum> editionClass = (Class<? extends Enum>) Class.forName(
                "tpi.dgrv4.common.utils.LicenseEditionType");
        Class<?> envClass = Class.forName("tpi.dgrv4.dpaa.constant.LicenseEnvType");
        Method method = utilClass.getDeclaredMethod(
                "quickGeneratorLicense", editionClass, int.class, int.class, int.class,
                String.class, envClass);

        method.setAccessible(true);
        method.invoke(null, Enum.valueOf(editionClass, "Alpha"), year, month, day, "TPI", null);
    }
}
