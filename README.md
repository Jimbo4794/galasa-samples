# Sample 2 Guide - Creating our Annotations

For our testers we want to create a annotation like: 
```
@MyApp (tag = "main", version = "12.34")
public IMyApp myApplication;
```
Which would give our tests an object inside the test that would represent our applicaton. The use of this annotation would go away and provision and configure our application ready for testing. I have also included a tag and version field that is optional but allows the tester to override.

So to start creating this annotation we need both the annoation class `MyApp.java` and the Object interface `IMyApp.java`. Starting with the interface:
```
public interface IMyApp {

    public String getTag();

    public String getVersion();
    
}
```
In this interface are all the methods that will be provided from the annotation to the tester. If you want you annotation object to provide a certain peice of functionality, it needs to be expressed in the interface. For now, ive kept it very simple and passed back the version and tag.

In the annotation class `MyApp.java`:
```
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@MyAppManagerField
@ValidAnnotatedFields({ IMyApp.class })
public @interface MyApp {

    public String tag() default "PRIMARY";

    public String version() default "latest";

}
```
You can see in this annotations there are several other annotations:
- @Retention states how long this annotation is to be retained, in this case: `Annotations are to be recorded in the class file by the compiler and retained by the VM at run time, so they may be read reflectively.`
- @Target indicates the contexts in which an annotation type is applicable.
- @MyAppManagerField is our annotation which states and groups other annotations to be owned by our manager
- @ValidAnnotatedFields({ IMyApp.class }) validates the object that is created is of toye IMyApp.class

Then inside our class we defined the both the tag and version, with default values.

Once we have these we need to actually create an implmentation of our object to be passed to the annotation. Again this is done in the internal package so not to be exposed (`MyAppImpl.java`):
```
public class MyAppImpl implements IMyApp {
    private String tag;
    private String version;

    public MyAppImpl(String tag, String version) {
        this.tag = tag;
        this.version = version;
    }

    @Override
    public String getTag() {
        return this.tag;
    }

    @Override
    public String getVersion() {
        return this.version;
    } 
}
```
For the time being this is a overly simple impl that allow us to get started. But with this we have all the pieces we now need to complete the provision generate step of our lifecycle. So going back to our `MyAppManagerImpl.java` class, I have created a couple of new methods:
```
@GenerateAnnotatedField(annotation = MyApp.class)
    public IMyApp generateMyApp(Field field, List<Annotation> annotations) throws MyAppException {
        MyApp myAppAnnotation = field.getAnnotation(MyApp.class);

        return new MyAppImpl(myAppAnnotation.tag(), myAppAnnotation.version());
    }
```
Which is repsonsible for creating the object for our @MyApp annotation. I also added the override for our provision generate step:
```
@Override
public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
    List<AnnotatedField> foundAnnotatedFields = findAnnotatedFields(MyAppManagerField.class);
    for (AnnotatedField annotatedField : foundAnnotatedFields) {
        Field field = annotatedField.getField();
        List<Annotation> annotations = annotatedField.getAnnotations();

        if (field.getType() == IMyApp.class) {
            MyApp annotation = field.getAnnotation(MyApp.class);
            if (annotation != null) {
                IMyApp myApp = generateMyApp(field, annotations);
                registerAnnotatedField(field, myApp);
            }
        }
    }
}
```
In this method we look for any annotations that are annotated with our `@MyAppManagerField` annotation. If we had multiple annotations in this manager then we would loop through all the type and try and distringuish them, just like we are doing for the MyApp annotation. We call for our annotation object to be created, registered and passed back to the test class as a now instantiated object.

At this point the manager intialise and provision are complete. For the sake of completeness I will add some basic functionality to our MyAppImpl.java class to show how it can be used to perform work. I wll then show a basic provision discard step.