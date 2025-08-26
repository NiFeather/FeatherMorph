package xyz.nifeather.morph.platform;

public interface IPlatformProvider<TNativePlayer, TNativeEntity, TNativeWorld, TNativeLocation>
{
    IPlatform<TNativeEntity, TNativePlayer, TNativeWorld, TNativeLocation> platform();
}
