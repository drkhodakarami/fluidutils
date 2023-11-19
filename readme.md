This is a simple yet powerful api mod. If you ever struggled with rendering your fluid container in Fabric, or tried transfering liquid from buck to container by using slots inside the block entity inventory and had to hard code the fluid and bucket types, this api is for you.

### What you get with this API?

- Fluid Stack : A nice way of having your fluid variant type and it's amount inside a wrapper class to use
- Fluid Stack Renderer : Thanks to [Kaupenjoe](https://www.youtube.com/@ModdingByKaupenjoe) this is a very powerful class library to render your fluid inside a screen container. Just instantiate a new instance of the class, and simple call the **drawFluid** in **drawBackground** (of screen class) or **getTooltip** in the **drawForeground** (of the screen class)! You can use **isMouseAboveArea** to check if the mouse is inside the drawing rectangle to show the tooltip or not.
- Fluid Utils : This is a very powerful helper class. The normal way for transferring liquid between a block entity container and a bucket, given by the Fabric, is to right click on the container by player. If the bucket is empty, it will be fulled and fluid will be reduced inside the container. If the bucket is full, it will become empty and the liquid will be transferred into the container. If this is the behavior you seek, you can look into >>> **FluidStorageUtil.interactWithFluidStorage(Storage<FluidVariant>, PlayerEntity, Hand)** <<< How ever, most of the time this is not the desired mechanics. What if we need to transfer multiple types of liquid into multiple tanks inside a single block entity container? Normally you would like to have a dedicated slot inside the screen for putting buckets (full or empty) in and transfer the liquid and get the result bucket inside a slot. If this is what you want, you can call >>> **handleTankTransfer** <<< method inside this class. It will first try to transfer liquid from bucket to tank, if it fails, it will try the reverse and tries to pull liquid from tank and put it inside the bucket. There are overloads for using milli bucket value system (from forge) and to handle the transfer on one side manually if needed. Other useful method calls can be used in different cases for checking the item stack, the tank and it's capacity.

To use this library inside your own project you need to add the maven repository like this, put this code inside repository seciton of the build.gradle file, Add it in your root build.gradle at the end of repositories:

```Maven Repository
repositories 
{
    maven { url 'https://jitpack.io' }
}
```

Now in the dependency section, add this :
```dependencies
dependencies 
{
    modImplementation "com.github.drkhodakarami:fluidutils:${fluidutils_version}"
}
```

finally, in the gradle.properties add an entry for the proper release version of the library :

[![Release](https://jitpack.io/v/drkhodakarami/fluidutils.svg)](https://jitpack.io/#drkhodakarami/fluidutils)
```gradle.properties
fluidutils_version=
```