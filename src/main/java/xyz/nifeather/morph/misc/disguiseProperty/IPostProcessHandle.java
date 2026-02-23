package xyz.nifeather.morph.misc.disguiseProperty;

@FunctionalInterface
public interface IPostProcessHandle<X>
{
    void handle(X value, PropertyHandler propertyHandler) throws ParseErrorException;
}
