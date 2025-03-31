Some TODO for this project:
1. **Unify *SingleProperty*, *SingleValue* and *CustomEntry*, and make them all together under one large data registry.**
   - THESE THINGS ARE TOTALLY DISASTER, WHY ARE WE HAVING THREE REGISTRIES TO HOLD ENTITY DATA?
2. Simply the registration for *SingleValue* and *SingleWatcher* maybe?
   - Currently， if we want to add a new property to a disguise:
     1. Create a matching *SingleProperty* and register to *DisguiseProperties*
     2. Write a *SingleValue* that matches the entity's datatracker values, and register to *ValueIndex*
     3. Write a *SingleWatcher* that uses the created *SingleValue*, and register to *WatcherIndex*
        - And this SingleWatcher will need to adapt both SingleValue and NBT
     4. Test and hope nothing is missed.
   - This was useful at that time, but now it kinda suck.