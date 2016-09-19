import java.applet.Applet;
import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Enumeration;

import javax.media.j3d.Appearance;
import javax.media.j3d.Background;
import javax.media.j3d.Behavior;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.Material;
import javax.media.j3d.PointLight;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Texture;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.media.j3d.WakeupCriterion;
import javax.media.j3d.WakeupOnAWTEvent;
import javax.vecmath.Color3f;
import javax.vecmath.Color4f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import com.sun.j3d.utils.applet.MainFrame;
import com.sun.j3d.utils.picking.PickResult;
import com.sun.j3d.utils.picking.behaviors.*;
import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.geometry.Cone;
import com.sun.j3d.utils.geometry.Cylinder;
import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;
import com.sun.j3d.utils.geometry.Primitive;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.geometry.Stripifier;
import com.sun.j3d.utils.image.TextureLoader;
import com.sun.j3d.utils.universe.SimpleUniverse;

import javax.media.j3d.TextureAttributes;

public class Project extends Applet implements MouseListener, MouseMotionListener {
   
   private SimpleUniverse universe;
   private TransformGroup chair;
   private BranchGroup theScene;
   private Canvas3D canvas;
   private Transform3D tfChair;
   private Transform3D temp;
   private OrbitBehavior ob;
   
   private Sphere lampGlobe;
   private Box tvScreen;
   private Cylinder floor;
   
   private Appearance tvScreenApp;
   private Material tvOn;   
   private Material tvOff;
   private Material lampOnMaterial;
   private Material lampOffMaterial;

   private PointLight lampLight;
   
   private double lastX = -1000;
   private double lastZ = -1000;
   private boolean chairSelected = false;
   private boolean lampOn = true;
   
   public static void main(String [] args){
      new MainFrame(new Project(), 1000, 800);
   }
   
   public void destroy(){
      universe.removeAllLocales();
   }  
   
   /*******************************************************************
   * 
   * INTERACTIVITY 1 - KEYBOARD CONTROLS FOR TV CHANNELS
   * 
   ******************************************************************/

   
   /*
    * A class that handles keyboard inputs that control the TV.
    * Keys 1, 2, 3, 9 and 0 can be used to switch between the TV "channels" (texture images).
    */
   
   public class KeyboardBehaviour extends Behavior {     
      KeyboardBehaviour() {
      }

      // wake on any key press
      public void initialize() {
         this.wakeupOn(new WakeupOnAWTEvent(KeyEvent.KEY_PRESSED));
      }

      // handle the AWTEvent - pass it on to be processed
      public void processStimulus(Enumeration criteria) {
         while(criteria.hasMoreElements()){
            WakeupCriterion wakeupElement = (WakeupCriterion) criteria.nextElement();       
            
            if (wakeupElement instanceof WakeupOnAWTEvent){   
               WakeupOnAWTEvent ev = (WakeupOnAWTEvent) wakeupElement;
               AWTEvent[] events = ev.getAWTEvent();
               processAWTEvent(events);  
            }
         }
         this.wakeupOn(new WakeupOnAWTEvent(KeyEvent.KEY_PRESSED));
      }
      
      //process the AWTevent - get the key, switch the "channel" on the TV
      private void processAWTEvent(AWTEvent[] events){
         for(int i = 0; i < events.length; i++){
            if(events[i] instanceof KeyEvent){
               KeyEvent eventKey = (KeyEvent) events[i];
         
               if(eventKey.getID() == KeyEvent.KEY_PRESSED){
                  int keyCode = eventKey.getKeyCode();
          
                  switch(keyCode){
                  
                  //turn the TV off
                  case KeyEvent.VK_0:
                     tvScreen.getAppearance().setTexture(null);
                     tvScreen.getAppearance().setMaterial(tvOff);
                     break;

                  //TV channel 1
                  case KeyEvent.VK_1:

                     TextureLoader key1tl = new TextureLoader("the_shawshank_redemption.png", new Container());     
                     Texture key1texture = key1tl.getTexture();
                     key1texture.setBoundaryModeS(Texture.CLAMP);
                     key1texture.setBoundaryModeT(Texture.CLAMP);
                     
                     tvScreen.getShape(Box.LEFT).getAppearance().setTexture(key1texture);
                     break;
                     
                  //TV channel 2
                  case KeyEvent.VK_2:

                     TextureLoader key2tl = new TextureLoader("John-ad_lrg.jpg", new Container());     
                     Texture key2texture = key2tl.getTexture();
                     key2texture.setBoundaryModeS(Texture.CLAMP);
                     key2texture.setBoundaryModeT(Texture.CLAMP);
                     
                     tvScreen.getShape(Box.LEFT).getAppearance().setTexture(key2texture);
                     break;
                     
                  //TV channel 3
                  case KeyEvent.VK_3:

                     TextureLoader key3tl = new TextureLoader("GameOfThrones.png", new Container());     
                     Texture key3texture = key3tl.getTexture();
                     key3texture.setBoundaryModeS(Texture.CLAMP);
                     key3texture.setBoundaryModeT(Texture.CLAMP);
                     
                     tvScreen.getShape(Box.LEFT ).getAppearance().setTexture(key3texture);
                     break;
                     
                  //TV channel 9
                  case KeyEvent.VK_9:
                     tvScreen.getAppearance().setTexture(null);
                     tvScreen.getAppearance().setMaterial(tvOn);
                     break;
                  }
               }
            }
         }
      }
   }   
   
   /*******************************************************************
   * 
   * INTERACTIVITY 2 - MOUSE CONTROLS (PICKING)
   * 
   ******************************************************************/
   
   
   /*
    * set up mouse picking controls - pick objects using the mouse
    */
   public void addMousePicking(){
      BoundingSphere bs = new BoundingSphere(new Point3d(0.0,0.0,0.0),10f);
      MousePickingCapability sp = new MousePickingCapability(canvas,theScene,bs);
      
      theScene.addChild(sp);      
   }
	
   //pick java3d shapes with the mouse
   public class MousePickingCapability extends PickMouseBehavior {
      
      public MousePickingCapability(Canvas3D pCanvas, BranchGroup root, Bounds pBounds) {
         super(pCanvas, root, pBounds);
         setSchedulingBounds(pBounds);
      }

      //locate and store the shape that was picked
      public void updateScene(int xpos, int ypos) {
         
         Primitive pickedShape = null;
         pickCanvas.setShapeLocation(xpos, ypos);
         PickResult pResult = pickCanvas.pickClosest();
         
         if (pResult != null)
            pickedShape = (Primitive) pResult.getNode(PickResult.PRIMITIVE);

         if (pickedShape != null && pickedShape.getUserData() != null) {            
            chairSelected = false;
            
            //the chair was selected, note it
            if(pickedShape.getUserData().equals("chair")){
               chairSelected = true;
            }
            
            //the lamp was selected, turn it off/on
            else if(pickedShape.getUserData().equals("lamp")){

               if(lampOn){
                  lampGlobe.getAppearance().setMaterial(lampOffMaterial);
                  lampLight.setInfluencingBounds(null);                  
               }else{
                  lampGlobe.getAppearance().setMaterial(lampOnMaterial);
                  lampLight.setInfluencingBounds(new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0));                 
               }
               lampOn = !lampOn;
            }               
         } else {
            chairSelected = false;
         }
      }
   }   
   
   /*******************************************************************
   * 
   * INTERACTIVITY 3 - MOUSE CONTROLS (DRAGGING AND MOVEMENT)
   * 
   ******************************************************************/
   
   
   /*
    * The following methods allow the user to move objects with the mouse.
    * They are only implemented for the chair in the scene.
    * A portion of code from INTERACTIVITY 2 is used here.
    * First, the object is picked, as before. Then, if it turns out to
    * be a chair it can be moved around the scene.
    * 
    * Camera movement is disabled while the chair is being moved because it uses
    * the same mouse keys.
    */
   
	@Override
	public void mouseClicked(MouseEvent arg0) {
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
	}

	@Override
	public void mousePressed(MouseEvent event) {
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
	   if(chairSelected){
         lastX = -1000;
         lastZ = -1000;    
	      chairSelected = false;
	      universe.getViewingPlatform().setViewPlatformBehavior(ob);
	      tfChair = temp; 	      
	   }
	}

	//control chair dragging
	@Override
	public void mouseDragged(MouseEvent event) {
		if (!chairSelected){
         return;
		}
		Point3d intersectionPoint = getPosition(event);  //map user click onto the floor
		
		//set up bounds for the chair - prevent it from moving too far from the centre of the scene
      if (Math.abs(intersectionPoint.x) < 5 && Math.abs(intersectionPoint.z) < 5)  {
         if (lastX == -1000) {
            universe.getViewingPlatform().setViewPlatformBehavior(null);   //block scene rotation when moving objects
            lastX = intersectionPoint.x;
            lastZ = intersectionPoint.z;
            return;
         }
         
         //move the chair according to mouse movement
         Vector3d transform = new Vector3d(intersectionPoint.x - lastX, 0, intersectionPoint.z - lastZ);
         temp = new Transform3D();
         temp.set(transform);
         temp.mul(tfChair);        
         chair.setTransform(temp); 
      }
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
	}
   	
	/*
	 * get the position of the mouse event. 
	 * Maps the position onto the scene floor where the mouse pointer intersects with a portion of the floor.
	 */
   public Point3d getPosition(MouseEvent event) {
	   Point3d eyePos = new Point3d();
	   Point3d mousePos = new Point3d();
	   
	   //use the "centre eye" for calculations
	   canvas.getCenterEyeInImagePlate(eyePos);    
	   
	   //compute the position of the specified AWT pixel value in image-plate coordinates
	   canvas.getPixelLocationInImagePlate(event.getX(), event.getY(), mousePos);     
	   
	   Transform3D transform = new Transform3D();
	   
	   //get the current transform from image plate coordinates to virtual world coordinates
	   canvas.getImagePlateToVworld(transform);
	   transform.transform(eyePos);
	   transform.transform(mousePos);
	   Vector3d direction = new Vector3d(eyePos);
	   direction.sub(mousePos);
	   
	   //the three points on the target surface (floor) 
      Point3d p1 = new Point3d(6f, .001f, -6f);
	   Point3d p2 = new Point3d(6f, .001f, 6f);
	   Point3d p3 = new Point3d(-6f, .001f, 6f);
	   Transform3D currentTransform = new Transform3D();
	   floor.getLocalToVworld(currentTransform);
	   currentTransform.transform(p1);
	   currentTransform.transform(p2);
	   currentTransform.transform(p3);	
	   
	   //find the point of intersection between the floor and mouse pos relative to the viewer position
	   Point3d intersection = getIntersection(eyePos, mousePos, p1, p2, p3);
	   
	   currentTransform.invert();
	   currentTransform.transform(intersection);
	   return intersection;		
   }
	
   /*
    * Returns the point where a line crosses a plane that is defined by 3 points. 
    * This method has been acquired from http://www.java3d.org/miceandmen.html
    */
   Point3d getIntersection(Point3d line1, Point3d line2, Point3d plane1, Point3d plane2, Point3d plane3) {
	   Vector3d p1 = new Vector3d(plane1);
	   Vector3d p2 = new Vector3d(plane2);
	   Vector3d p3 = new Vector3d(plane3);
	   Vector3d p2minusp1 = new Vector3d(p2);
	   p2minusp1.sub(p1);
	   Vector3d p3minusp1 = new Vector3d(p3);
	   p3minusp1.sub(p1);
	   Vector3d normal = new Vector3d();
	   normal.cross(p2minusp1, p3minusp1);
	   
	   double d = -p1.dot(normal);
	   Vector3d i1 = new Vector3d(line1);
	   Vector3d direction = new Vector3d(line1);
	   direction.sub(line2);
	   double dot = direction.dot(normal);
	   if (dot == 0) return null;
	   double t = (-d - i1.dot(normal)) / (dot);
	   Vector3d intersection = new Vector3d(line1);
	   Vector3d scaledDirection = new Vector3d(direction);
	   scaledDirection.scale(t);
	   intersection.add(scaledDirection);
	   Point3d intersectionPoint = new Point3d(intersection);
	   return intersectionPoint;
   }
	
   public void init(){
      setLayout(new BorderLayout());
		
      canvas = new Canvas3D(SimpleUniverse.getPreferredConfiguration());
      add("Center", canvas);
		
      universe = new SimpleUniverse(canvas);	
      theScene = new BranchGroup();
		
      //set scene bounds
      Point3d sceneCenter = new Point3d(-1f, 2f, 4f);
      double sceneBoundsRadius = .5f;
      BoundingSphere sceneBounds = new BoundingSphere(sceneCenter, sceneBoundsRadius);
      theScene.setBounds(sceneBounds);
		
      // set the starting view platform				
      Transform3D viewTransform = new Transform3D();
      
      viewTransform.lookAt(sceneCenter, new Point3d(0,0,0), new Vector3d(0,1,0));
      viewTransform.invert();
      
      universe.getViewingPlatform().getViewPlatformTransform().setTransform(viewTransform);
      
      //set up keyboard controls for the TV
      KeyboardBehaviour myKeyboardBehaviour = new KeyboardBehaviour();
      myKeyboardBehaviour.setSchedulingBounds(new BoundingSphere());
      theScene.addChild(myKeyboardBehaviour);
				
      //add scene elements
      createAppearances();
      createModels();
      addLight();		
      addMousePicking();
      finaliseScene();
      
      canvas.addMouseMotionListener(this);
      canvas.addMouseListener(this);
		
      //allow mouse controls to move the camera
      ob = new OrbitBehavior(canvas);
      ob.setSchedulingBounds(new BoundingSphere(new Point3d(0,0,0),Double.MAX_VALUE));
      universe.getViewingPlatform().setViewPlatformBehavior(ob);
   }
   
   public void finaliseScene(){
      theScene.compile();
      universe.addBranchGraph(theScene);
   }
	
   //sets up global appearances
   public void createAppearances(){
      tvScreenApp = new Appearance();

      tvOn = new Material();
      tvOn.setEmissiveColor(new Color3f(0.2f,0.33f,0.71f));
      tvOn.setShininess(70);
      
      tvOff = new Material();
      tvOff.setDiffuseColor(new Color3f(0.01f,0.01f,0.01f));
      tvOff.setSpecularColor(new Color3f(0.72f,0.72f,0.72f));
      tvOff.setShininess(50);     
      
      TextureAttributes tvAttr = new TextureAttributes();
      tvAttr.setTextureMode(TextureAttributes.REPLACE);
      tvScreenApp.setTextureAttributes(tvAttr);
      tvScreenApp.setMaterial(tvOn);      
      tvScreenApp.setCapability(Appearance.ALLOW_MATERIAL_READ);
      tvScreenApp.setCapability(Appearance.ALLOW_MATERIAL_WRITE);
      tvScreenApp.setCapability(Appearance.ALLOW_TEXTURE_READ);
      tvScreenApp.setCapability(Appearance.ALLOW_TEXTURE_WRITE); 
      tvScreenApp.setCapability(Appearance.ALLOW_TEXTURE_ATTRIBUTES_READ);
      tvScreenApp.setCapability(Appearance.ALLOW_TEXTURE_ATTRIBUTES_WRITE);   

      lampOffMaterial = new Material();   
      lampOffMaterial.setEmissiveColor(new Color3f(.5f,.5f,.5f));  
      lampOffMaterial.setShininess(100);
      
      lampOnMaterial = new Material();
      lampOnMaterial.setEmissiveColor(new Color3f(1f,1f,1f));   
      lampOnMaterial.setShininess(100);
   }
   
   public void createModels(){
      createTable();
      createTV();
      createLamp();
      createChair();
      createFloor();
      createBackground();
   }
	
   public void createTable(){
      //appearance
      Appearance tableApp = new Appearance();
      Material blackGlass = new Material();
      blackGlass.setAmbientColor(new Color3f(0.2f,0.2f,0.2f));
      blackGlass.setEmissiveColor(new Color3f(0f,0f,0f));
      blackGlass.setDiffuseColor(new Color3f(0.05f,0.05f,0.05f));
      blackGlass.setSpecularColor(new Color3f(0.72f,0.72f,0.72f));
      blackGlass.setShininess(23);
      tableApp.setMaterial(blackGlass);
		
      //table top dimensions
      float tableTopXdim = .5f;
      float tableTopZdim = .2f;
		
      
      /*******************************************************************
      * 
      * LEGS
      * 
      ******************************************************************/
		
      //table leg dimensions
      float tableLegHeight = 0.46f;
      float tableFrontLegRadius = 0.025f;
      float tableBackLegRadius = tableFrontLegRadius/2;
      float tableLegDistanceToSide = 0.04f;
		
      //leg positions
      float tableFrontLegXPos = tableTopXdim - tableLegDistanceToSide;
      float tableLegYPos = 0f;
      float tableLegZPos = tableTopZdim - tableLegDistanceToSide;		
      float tableBackLegXPos = tableFrontLegXPos * 1/5;
		
      Cylinder tableLeg1 = new Cylinder(tableFrontLegRadius, tableLegHeight, tableApp);
      Cylinder tableLeg2 = new Cylinder(tableFrontLegRadius, tableLegHeight, tableApp);
      Cylinder tableLeg3 = new Cylinder(tableBackLegRadius, tableLegHeight, tableApp);
      Cylinder tableLeg4 = new Cylinder(tableBackLegRadius, tableLegHeight, tableApp);
		
      Vector3f tableLeg1Trans = new Vector3f(-tableFrontLegXPos, tableLegYPos, tableLegZPos);
      Vector3f tableLeg2Trans = new Vector3f(tableFrontLegXPos, tableLegYPos, tableLegZPos);
      Vector3f tableLeg3Trans = new Vector3f(tableBackLegXPos, tableLegYPos, -tableLegZPos);
      Vector3f tableLeg4Trans = new Vector3f(-tableBackLegXPos, tableLegYPos, -tableLegZPos);

      //move each table leg into its position relative to other table legs
      Transform3D tfTableLeg1 = new Transform3D();
      tfTableLeg1.setTranslation(tableLeg1Trans);		
      TransformGroup tgTableLeg1 = new TransformGroup(tfTableLeg1);
      tgTableLeg1.addChild(tableLeg1);
		
      Transform3D tfTableLeg2 = new Transform3D();
      tfTableLeg2.setTranslation(tableLeg2Trans);		
      TransformGroup tgTableLeg2 = new TransformGroup(tfTableLeg2);
      tgTableLeg2.addChild(tableLeg2);

      Transform3D tfTableLeg3 = new Transform3D();
      tfTableLeg3.setTranslation(tableLeg3Trans);		
      TransformGroup tgTableLeg3 = new TransformGroup(tfTableLeg3);
      tgTableLeg3.addChild(tableLeg3);
		
      Transform3D tfTableLeg4 = new Transform3D();
      tfTableLeg4.setTranslation(tableLeg4Trans);		
      TransformGroup tgTableLeg4 = new TransformGroup(tfTableLeg4);
      tgTableLeg4.addChild(tableLeg4);
		
      //gather all table legs into one TransformGroup to be moved as a unit
      TransformGroup tgTableLegs = new TransformGroup();
      tgTableLegs.addChild(tgTableLeg1);
      tgTableLegs.addChild(tgTableLeg2);
      tgTableLegs.addChild(tgTableLeg3);
      tgTableLegs.addChild(tgTableLeg4);

				
      /*******************************************************************
      * 
      * TABLE TOP
      * 
      ******************************************************************/
				
      //The vertices of the table.
      GeometryInfo tableGeom = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
      Point3f[] tableCoordinates =
      {
         //bottom surface
         new Point3f(0f,   0f, 0f),
         new Point3f(1f,   0f, 0f),
         new Point3f(1f,   0f, .25f),
         new Point3f(.85f, 0f, .4f),
         new Point3f(.15f, 0f, .4f),
         new Point3f(0f,   0f, .25f),
	
         //top surface - added y-axis thickness
         new Point3f(0f,   .01f, 0f),
         new Point3f(1f,   .01f, 0f),
         new Point3f(1f,   .01f, .25f),
         new Point3f(.85f, .01f, .4f),
         new Point3f(.15f, .01f, .4f),
         new Point3f(0f,   .01f, .25f),
      };    
	    
      //The 8 faces of the table.
      int tableCoordIndices[] =
      {
         //bottom suraface
         0,1,2,3,4,5,
         
         //sides
         0,6,7,1,
         1,7,8,2,
         2,8,9,3,
         3,9,10,4,
         4,10,11,5,
         5,11,6,0,
         
         //top surface
         11,10,9,8,7,6       
      };
      
      // two hexagons (top/bottom), six rectangles (sides)
      int[] tableStripCounts = {6,4,4,4,4,4,4,6};
      
      tableGeom.setStripCounts(tableStripCounts);	    
      tableGeom.setCoordinates(tableCoordinates);
      tableGeom.setCoordinateIndices(tableCoordIndices);

      new NormalGenerator().generateNormals(tableGeom);
      new Stripifier().stripify(tableGeom);
	   
      //create the shape defined by the geometry above       
      Shape3D tableTop = new Shape3D(tableGeom.getGeometryArray(), tableApp);
	    		
      //rotation and translation
      Transform3D tableTopTransform = new Transform3D();
      Transform3D tableTopTempTransform = new Transform3D();
	  	
      tableTopTransform.rotY(Math.PI);
	  	
      Vector3f tableTopTempTrans = new Vector3f(-tableTopXdim, tableLegHeight/2, -tableTopZdim);
      tableTopTempTransform.setTranslation(tableTopTempTrans);		
	  	
      //combine rotation and translation
      tableTopTransform.mul(tableTopTempTransform);
	  	
      TransformGroup tgTableTop = new TransformGroup(tableTopTransform);
      tgTableTop.addChild(tableTop);	    	     	    	       

				
      /*******************************************************************
      * 
      * SUB-TABLE 1 - THE MIDDLE SHELF
      * 
      ******************************************************************/
	  	
      //height of the middle shelf, relative to the top table
      float subtable1DistanceYToTop = -0.2f;

      Shape3D subtable1 = new Shape3D(tableGeom.getGeometryArray(), tableApp);
	         
      Transform3D subtable1Transform = new Transform3D(tableTopTransform);
      Transform3D subtable1TempTransform = new Transform3D();
      
      Vector3f subtableTrans = new Vector3f(0f, subtable1DistanceYToTop, 0f);
      subtable1TempTransform.setTranslation(subtableTrans);
	  	
      subtable1Transform.mul(subtable1TempTransform);		
		
      //scale - the middle shelf is half the height of the top table
      Transform3D subtableScale = new Transform3D();
      subtableScale.setScale(new Vector3d(1, .5, 1));	  	

      subtable1Transform.mul(subtableScale);		
	  	
      TransformGroup tgSubtable1 = new TransformGroup(subtable1Transform);
      tgSubtable1.addChild(subtable1);

				
      /*******************************************************************
      * 
      * SUB-TABLE 2 - BOTTOM SHELF
      * 
      ******************************************************************/
      
      //height of the bottom shelf, relative to the top table
      float subtable2DistanceYToTop = subtable1DistanceYToTop*2;
      
      Shape3D subtable2 = new Shape3D(tableGeom.getGeometryArray(), tableApp);

      Transform3D subtable2Transform = new Transform3D(tableTopTransform);
      Transform3D subtable2TempTransform = new Transform3D();

      Vector3f subtable2Trans = new Vector3f(0f, subtable2DistanceYToTop, 0f);
      subtable2TempTransform.setTranslation(subtable2Trans);
	  	
      subtable2Transform.mul(subtable2TempTransform);		

      subtable1Transform.mul(subtableScale);		
	  	
      TransformGroup tgSubtable2 = new TransformGroup(subtable2Transform);
      tgSubtable2.addChild(subtable2);

				
      /*******************************************************************
      * 
      * TABLE BACK
      * 
      ******************************************************************/

      //back dimensions
      float tableBackXdim = tableBackLegXPos;
      float tableBackYdim = .2f;
      float tableBackZdim = .001f;
		
      /*
      * account for the fact that all primitive shapes are built around the origin as well as  
      * the fact that (dim == 2*actual size) -> an object of xDim = 2 will have a size of 4, 
      * spanning from x=-2 to x=2, with the centre at x=0.
      */
      float tableBackXTransform = 0f;
      float tableBackYTransform = (tableLegHeight - tableBackYdim*2)/2;
      float tableBackZTransform = -tableLegZPos + tableBackLegRadius;
		
      Box tableBack = new Box(tableBackXdim, tableBackYdim, tableBackZdim, tableApp);

      Vector3f tableBackTrans = new Vector3f(tableBackXTransform, tableBackYTransform, tableBackZTransform);
		
      Transform3D tfTableBack = new Transform3D();
      tfTableBack.setTranslation(tableBackTrans);		
      TransformGroup tgTableBack = new TransformGroup(tfTableBack);
      tgTableBack.addChild(tableBack);

		
      /*******************************************************************
      * 
      * PUTTING THE TABLE PARTS TOGETHER
      * 
      ******************************************************************/
		
      //bring the table to the "ground level", where the lowest point on the Y axis is 0
      Vector3f tableTrans = new Vector3f(0f, tableLegHeight/2, 0f);
		
      Transform3D tfTable = new Transform3D();
      tfTable.setTranslation(tableTrans);		
      
      TransformGroup table = new TransformGroup(tfTable);
      table.addChild(tgTableLegs);
      table.addChild(tgTableTop);
      table.addChild(tgSubtable1);
      table.addChild(tgSubtable2);
      table.addChild(tgTableBack);
		
      theScene.addChild(table);
   }
   
   public void createTV(){
      //appearance
      Appearance tvApp = new Appearance();
      Material blackPlastic = new Material();
      blackPlastic.setAmbientColor(new Color3f(0.2f,0.2f,0.2f));
      blackPlastic.setEmissiveColor(new Color3f(0f,0f,0f));
      blackPlastic.setDiffuseColor(new Color3f(0f,0f,0f));
      blackPlastic.setSpecularColor(new Color3f(0.85f,0.85f,0.85f));
      blackPlastic.setShininess(22);
      tvApp.setMaterial(blackPlastic);
	  	

      /*******************************************************************
      * 
      * TV BASE
      * 
      ******************************************************************/
      
      //dimensions
      float tvBaseWidth = .5f;
      float tvBaseHeight = .02f;
      float tvBaseDepth = .25f;
      
      GeometryInfo tvBaseGeom = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
      Point3f[] tvBaseCoordinates =
      {		        
         //lowest level
         new Point3f(0f,   0f, 0f),
         new Point3f(.5f,   0f, 0f),
         new Point3f(.5f,   0f, -.15f),
         new Point3f(.375f,   0f, -.25f),
         new Point3f(.125f,   0f, -.25f),
         new Point3f(0f, 0f, -.15f),
		        
         //mid level
         new Point3f(0f,   .01f, 0f),
         new Point3f(.5f,   .01f, 0f),
         new Point3f(.5f,   .01f, -.15f),
         new Point3f(.375f,   .01f, -.25f),
         new Point3f(.125f,   .01f, -.25f),
         new Point3f(0f, .01f, -.15f),
		        
         //upper level
         new Point3f(.03f,   .02f, -0.03f),
         new Point3f(.47f,   .02f, -0.03f),
         new Point3f(.47f,   .02f, -.12f),
         new Point3f(.345f,   .02f, -.22f),
         new Point3f(.155f,   .02f, -.22f),
         new Point3f(.03f, .02f, -.12f),
      };    
	    
      //The 14 surfaces of the case.
      int tvBaseCoordIndices[] =
      {
         //lowest level
         5,4,3,2,1,0,
         
         //mid level
         1,7,6,0,
         2,8,7,1,
         3,9,8,2,
         4,10,9,3,
         5,11,10,4,
         0,6,11,5,
         
         //upper slanted edge
         7,13,12,6,
         8,14,13,7,
         9,15,14,8,
         10,16,15,9,
         11,17,16,10,
         6,12,17,11,
         
         //top
         12,13,14,15,16,17
      };
	    		
      // two hexagons (top/bottom), 12 quadrilaterals (sides/slanted edge)
      int[] tvBaseStripCounts = {6,4,4,4,4,4,4,4,4,4,4,4,4,6};
      
      tvBaseGeom.setStripCounts(tvBaseStripCounts);	    
      tvBaseGeom.setCoordinates(tvBaseCoordinates);
      tvBaseGeom.setCoordinateIndices(tvBaseCoordIndices);

      new NormalGenerator().generateNormals(tvBaseGeom);
      new Stripifier().stripify(tvBaseGeom);
	    	       
      Shape3D tvBase = new Shape3D(tvBaseGeom.getGeometryArray(), tvApp);
	    
      
      /*******************************************************************
      * 
      * TV CONNECTOR - JOINS THE BASE TO THE TV CASE
      * 
      ******************************************************************/
      
      float tvConnectorWidth =  .06f;
      float tvConnectorHeight = .02f;
      float tvConnectorLength = .01f;
	    
      Box tvConnector = new Box(tvConnectorWidth, tvConnectorHeight, tvConnectorLength, tvApp);

      Transform3D tvConnectorTransform = new Transform3D();	  	
		  	
      Vector3f tvConnectorTrans = new Vector3f(tvBaseWidth/2, tvConnectorHeight+tvBaseHeight, -tvBaseDepth/2.5f - tvConnectorLength);
      tvConnectorTransform.setTranslation(tvConnectorTrans);		  	
		  	
      TransformGroup tgTvConnector = new TransformGroup(tvConnectorTransform);
      tgTvConnector.addChild(tvConnector);	    	   
	  	
      Transform3D tvBaseTransform = new Transform3D();	  	  	
		  	
      //a single transformgroup for the base and the connector
      TransformGroup tgTvBase = new TransformGroup(tvBaseTransform);
      tgTvBase.addChild(tvBase);	    	   
      tgTvBase.addChild(tgTvConnector);	  
		
		
      /*******************************************************************
      * 
      * TV CASE
      * 
      ******************************************************************/
      
      float tvCaseWidth = 0.92f;
      
      GeometryInfo tvCaseGeom = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
      Point3f[] tvCaseCoordinates =
      {		        
         //front
         new Point3f(0f,   0f, 0f),
         new Point3f(.92f,   0f, 0f),
         new Point3f(.92f,   .56f, 0f),
         new Point3f(0f, .56f, 0f),
		        
         //middle
         new Point3f(0f,   0f, -.02f),
         new Point3f(.92f,   0f, -.02f),
         new Point3f(.92f,   .56f, -.02f),
         new Point3f(0f, .56f, -.02f),
		        
         //slanted back
         new Point3f(.08f, .06f, -.03f),
         new Point3f(.84f,   .06f, -.03f),	
         new Point3f(.84f,   .5f, -.03f),
         new Point3f(.08f, .5f, -.03f)
      };    
	    
      //The 10 surfaces of the case.
      int tvCaseCoordIndices[] =
      {
         //front
         0,1,2,3,
         
         //sides
         0,4,5,1,
         1,5,6,2,
         2,6,7,3,
         3,7,4,0,
         
         //back - slanted
         4,8,9,5,
         5,9,10,6,
         6,10,11,7,
         7,11,8,4, 
         
         //back
         11,10,9,8
      };
	    		
      //ten quadrilaterals - front, 4 sides, 4 slanted sides, back
      int[] tvCaseStripCounts = {4,4,4,4,4,4,4,4,4,4};
      tvCaseGeom.setStripCounts(tvCaseStripCounts);
	    
      tvCaseGeom.setCoordinates(tvCaseCoordinates);
      tvCaseGeom.setCoordinateIndices(tvCaseCoordIndices);

      new NormalGenerator().generateNormals(tvCaseGeom);
      new Stripifier().stripify(tvCaseGeom);
	    	       
      Shape3D tvCase = new Shape3D(tvCaseGeom.getGeometryArray(), tvApp); 
	    
      Transform3D tvCaseTransform = new Transform3D();	
	  	
      TransformGroup tgTvCase = new TransformGroup(tvCaseTransform); 	
      tgTvCase.addChild(tvCase);
	  	
	  	
      /*******************************************************************
      * 
      * TV SCREEN
      * 
      ******************************************************************/  	   
      		
      //dimensions
      float tvScreenXdim = .45f;
      float tvScreenYdim = .27f;
      float tvScreenZdim = .0001f;
	  	
      tvScreen = new Box(tvScreenXdim, tvScreenYdim, tvScreenZdim, Box.GENERATE_TEXTURE_COORDS + Box.GENERATE_NORMALS, tvScreenApp);
      tvScreen.setCapability(Box.ENABLE_APPEARANCE_MODIFY);
	  	  		
      Vector3f tvScreenTrans = new Vector3f(tvScreenXdim +0.01f,tvScreenYdim+0.01f,-tvScreenZdim/2);
      
      Transform3D tvScreenTransform = new Transform3D();	
      tvScreenTransform.setTranslation(tvScreenTrans);		  	
	  	
      TransformGroup tgTvScreen = new TransformGroup(tvScreenTransform);
      tgTvScreen.addChild(tvScreen);
	  	
      
      /*******************************************************************
      * 
      * TV CASE + SCREEN
      * 
      ******************************************************************/  	  
	  	
      Vector3f tvCaseScreenTrans = new Vector3f(-tvCaseWidth/2+ tvBaseWidth/2, tvConnectorHeight*2+tvBaseHeight, -tvBaseDepth/2.5f);
      
      Transform3D tvCaseScreenTransform = new Transform3D();	
      tvCaseScreenTransform.setTranslation(tvCaseScreenTrans);		  	
	  	
      TransformGroup tgCaseAndScreen = new TransformGroup(tvCaseScreenTransform);
      tgCaseAndScreen.addChild(tgTvCase);	 
      tgCaseAndScreen.addChild(tgTvScreen);	   	 
	  	
	  	
      /*******************************************************************
      * 
      * PUTTING THE TV PARTS TOGETHER
      * 
      ******************************************************************/
		
      //put the TV on the table
      Vector3f tvTrans = new Vector3f(tvBaseWidth/2 - .5f, .47f, .4f/3);
		
      Transform3D tfTV = new Transform3D();
      tfTV.setTranslation(tvTrans);				
      TransformGroup tv = new TransformGroup(tfTV);
      tv.addChild(tgTvBase);
      tv.addChild(tgCaseAndScreen);
		  
      theScene.addChild(tv);		    
	}
   
   public void createLamp(){
      //appearances
      Appearance metallicApp = new Appearance();
      Material aluminium = new Material();
      aluminium.setDiffuseColor(new Color3f(0.37f,0.37f,0.37f));
      aluminium.setSpecularColor(new Color3f(0.89f,0.89f,0.89f));
      aluminium.setShininess(17);
      metallicApp.setMaterial(aluminium);
		
      //to turn off the light, set emissive colour to .5 AND turn off the associated point light
      Appearance glassApp = new Appearance();		
      glassApp.setCapability(Appearance.ALLOW_MATERIAL_READ);
      glassApp.setCapability(Appearance.ALLOW_MATERIAL_WRITE);
      glassApp.setMaterial(lampOnMaterial);
		
      //glass transparency
      TransparencyAttributes lampTA = new TransparencyAttributes();
      lampTA.setTransparencyMode (TransparencyAttributes.NICEST);
      lampTA.setTransparency(.02f);
      glassApp.setTransparencyAttributes(lampTA);
		
      //lamp dimensions
      float lampHeight = 1.25f;

				
      /*******************************************************************
      * 
      * THE GLOBE
      * 
      ******************************************************************/
      
      float lampGlobeRadius = .15f;
		
      lampGlobe = new Sphere(lampGlobeRadius, Sphere.GENERATE_NORMALS + Sphere.ENABLE_APPEARANCE_MODIFY, 100, glassApp);
      lampGlobe.setUserData("lamp");
		
      Vector3f lampGlobeTrans = new Vector3f(0f, lampHeight-lampGlobeRadius, 0f);
		
      Transform3D tfLampGlobe = new Transform3D();
      tfLampGlobe.setTranslation(lampGlobeTrans);		
      TransformGroup tgLampGlobe = new TransformGroup(tfLampGlobe);
      tgLampGlobe.addChild(lampGlobe);

				
      /*******************************************************************
      * 
      * THE BODY
      * 
      ******************************************************************/

      float lampBodyRadius = 0.01f;
      float lampBodyHeight = lampHeight - lampGlobeRadius*2;
		
      Cylinder lampBody = new Cylinder(lampBodyRadius, lampBodyHeight, metallicApp);
      lampBody.setUserData("lamp");
		
      Vector3f lampBodyTrans = new Vector3f(0f, lampBodyHeight/2, 0f);
		
      Transform3D tfLampBody = new Transform3D();
      tfLampBody.setTranslation(lampBodyTrans);		
      TransformGroup tgLampBody = new TransformGroup(tfLampBody);
      tgLampBody.addChild(lampBody);
		
		
      /*******************************************************************
      * 
      * THE BASE
      * 
      ******************************************************************/

      float lampBaseRadius = lampGlobeRadius;
      float lampBaseHeight = lampHeight/14;
		
      Cone lampBase = new Cone(lampBaseRadius, lampBaseHeight, metallicApp);
      lampBase.setUserData("lamp");
		
      Vector3f lampBaseTrans = new Vector3f(0f, lampBaseHeight/2, 0f);
		
      Transform3D tfLampBase = new Transform3D();
      tfLampBase.setTranslation(lampBaseTrans);		
      TransformGroup tgLampBase = new TransformGroup(tfLampBase);
      tgLampBase.addChild(lampBase);
		
		
      /*******************************************************************
      * 
      * THE GLOBE COVER
      * 
      ******************************************************************/

      float lampCoverRadius = lampGlobeRadius/2;
      float lampCoverHeight = lampGlobeRadius/2.5f;
		
      Cone lampCover = new Cone(lampCoverRadius, lampCoverHeight, metallicApp);
      lampCover.setUserData("lamp");
  		
      //rotate by 180 around the x axis
      Transform3D lampCoverTransform = new Transform3D();
      lampCoverTransform.rotX(Math.PI);
      
      Transform3D lampCoverTempTransform = new Transform3D();	  	
      Vector3f lampCoverTrans = new Vector3f(0f, -(lampHeight - lampGlobeRadius*2.05f), 0f);
      lampCoverTempTransform.setTranslation(lampCoverTrans);		
	  	
      //combine rotation and translation
      lampCoverTransform.mul(lampCoverTempTransform);
	  	
      TransformGroup tgLampCover = new TransformGroup(lampCoverTransform);
      tgLampCover.addChild(lampCover);
		
      
      /*******************************************************************
      * 
      * PUTTING THE LAMP PARTS TOGETHER
      * 
      ******************************************************************/
		
      Transform3D tfLamp = new Transform3D();
      Vector3f lampTrans = new Vector3f((lampHeight+lampGlobeRadius)/2, 0f, .7f);
      tfLamp.setTranslation(lampTrans);				
      
      TransformGroup lamp = new TransformGroup(tfLamp);
      lamp.addChild(tgLampGlobe);
      lamp.addChild(tgLampBody);
      lamp.addChild(tgLampBase);
      lamp.addChild(tgLampCover);
		
      theScene.addChild(lamp);		
   }
   
   public void createChair(){      
      //load the texture for the chair
      TextureLoader chairTextureLoader = new TextureLoader("wood_text.jpg", "LUMINANCE_ALPHA", new Container());		
      Texture chairTexture = chairTextureLoader.getTexture();
      chairTexture.setBoundaryModeS(Texture.WRAP);
      chairTexture.setBoundaryModeT(Texture.WRAP);
      chairTexture.setBoundaryColor(new Color4f(0.0f, 1.0f, 0.0f, 0.0f));
		
      TextureAttributes texAttr = new TextureAttributes();
      texAttr.setTextureMode(TextureAttributes.MODULATE);
      
      //set up the appearance
      Appearance chairApp = new Appearance();
      chairApp.setTexture(chairTexture);
      chairApp.setTextureAttributes(texAttr);
		
      Material wood = new Material();
      wood.setDiffuseColor(new Color3f(1.0f, 1.0f, 1.0f));
      wood.setEmissiveColor(new Color3f(.3f, .3f, .3f));
      wood.setShininess(80);
		
      chairApp.setMaterial(wood);
      
      float chairWidth = 0.44f;

				
      /*******************************************************************
      * 
      * THE LEGS
      * 
      ******************************************************************/

      //dimensions
      float chairLegHeight = .45f;
      float chairLegWidth = 0.04f;
				   	       
      // create and return shape
      Box chairLeg1 = new Box(chairLegWidth/2, chairLegHeight/2, chairLegWidth/2, Primitive.GENERATE_NORMALS + Primitive.GENERATE_TEXTURE_COORDS, chairApp);	  
      Box chairLeg2 = new Box(chairLegWidth/2, chairLegHeight/2, chairLegWidth/2, Primitive.GENERATE_NORMALS + Primitive.GENERATE_TEXTURE_COORDS, chairApp);	  
      Box chairLeg3 = new Box(chairLegWidth/2, chairLegHeight/2, chairLegWidth/2, Primitive.GENERATE_NORMALS + Primitive.GENERATE_TEXTURE_COORDS, chairApp);	 
      Box chairLeg4 = new Box(chairLegWidth/2, chairLegHeight/2, chairLegWidth/2, Primitive.GENERATE_NORMALS + Primitive.GENERATE_TEXTURE_COORDS, chairApp);	 

      chairLeg1.setUserData("chair");
      chairLeg2.setUserData("chair");
      chairLeg3.setUserData("chair");
      chairLeg4.setUserData("chair");
      
      Vector3f chairLeg1Trans = new Vector3f(chairWidth/2 - chairLegWidth/2, chairLegHeight/2, chairLegWidth/2);
      Vector3f chairLeg2Trans = new Vector3f(-chairWidth/2 + chairLegWidth/2, chairLegHeight/2, chairLegWidth/2);
      Vector3f chairLeg3Trans = new Vector3f(chairWidth/2 - chairLegWidth/2, chairLegHeight/2, -chairLegWidth/2 + chairWidth);
      Vector3f chairLeg4Trans = new Vector3f(-chairWidth/2 + chairLegWidth/2, chairLegHeight/2, -chairLegWidth/2 + chairWidth);
		
      Transform3D tfChairLeg1 = new Transform3D();
      tfChairLeg1.setTranslation(chairLeg1Trans);		
      TransformGroup tgChairLeg1 = new TransformGroup(tfChairLeg1);
      tgChairLeg1.addChild(chairLeg1);		
		
      Transform3D tfChairLeg2 = new Transform3D();
      tfChairLeg2.setTranslation(chairLeg2Trans);		
      TransformGroup tgChairLeg2 = new TransformGroup(tfChairLeg2);
      tgChairLeg2.addChild(chairLeg2);		
		
      Transform3D tfChairLeg3 = new Transform3D();
      tfChairLeg3.setTranslation(chairLeg3Trans);		
      TransformGroup tgChairLeg3 = new TransformGroup(tfChairLeg3);
      tgChairLeg3.addChild(chairLeg3);
		
      Transform3D tfChairLeg4 = new Transform3D();
      tfChairLeg4.setTranslation(chairLeg4Trans);		
      TransformGroup tgChairLeg4 = new TransformGroup(tfChairLeg4);
      tgChairLeg4.addChild(chairLeg4);
		
      TransformGroup tgChairLegs = new TransformGroup();
      tgChairLegs.addChild(tgChairLeg1);
      tgChairLegs.addChild(tgChairLeg2);
      tgChairLegs.addChild(tgChairLeg3);
      tgChairLegs.addChild(tgChairLeg4);

				
      /*******************************************************************
      * 
      * THE SEAT
      * 
      ******************************************************************/
				
      float chairSeatHeight = .05f;
      Box chairSeat = new Box(chairWidth/2, chairSeatHeight/2, chairWidth/2, Primitive.GENERATE_NORMALS + Primitive.GENERATE_TEXTURE_COORDS, chairApp);
      chairSeat.setUserData("chair");
      
      //account for (dim == 1/2 * size)
      Vector3f chairSeatTrans = new Vector3f(0f, chairSeatHeight/2+chairLegHeight, chairWidth/2);
		
      Transform3D tfChairSeat = new Transform3D();
      tfChairSeat.setTranslation(chairSeatTrans);		
      TransformGroup tgChairSeat = new TransformGroup(tfChairSeat);
      tgChairSeat.addChild(chairSeat);

				
      /*******************************************************************
      * 
      * THE BACK
      * 
      ******************************************************************/
				
      float chairBackHeight = .4f;
      float chairHeadrestHeight = .14f;
      float chairBackWidth = .04f;
      float chairSegment = (chairWidth-chairBackWidth*2)/7;

      Box chairBack1 = new Box(chairBackWidth/2, chairBackHeight/2, chairBackWidth/2, Primitive.GENERATE_NORMALS + Primitive.GENERATE_TEXTURE_COORDS, chairApp);
      chairBack1.setUserData("chair");
      Vector3f chairBack1Trans = new Vector3f(0f, 0f, 0f);
      Transform3D tfChairBack1 = new Transform3D();
      tfChairBack1.setTranslation(chairBack1Trans);		
      TransformGroup tgChairBack1 = new TransformGroup(tfChairBack1);
      tgChairBack1.addChild(chairBack1);	

      Box chairBack2 = new Box(chairBackWidth/2, chairBackHeight/2, chairBackWidth/2, Primitive.GENERATE_NORMALS + Primitive.GENERATE_TEXTURE_COORDS, chairApp);
      chairBack2.setUserData("chair");
      Vector3f chairBack2Trans = new Vector3f(chairSegment*2, 0f, 0f);
      Transform3D tfChairBack2 = new Transform3D();
      tfChairBack2.setTranslation(chairBack2Trans);		
      TransformGroup tgChairBack2 = new TransformGroup(tfChairBack2);
      tgChairBack2.addChild(chairBack2);		

      Box chairBack3 = new Box(chairBackWidth/2, chairBackHeight/2, chairBackWidth/2, Primitive.GENERATE_NORMALS + Primitive.GENERATE_TEXTURE_COORDS, chairApp);
      chairBack3.setUserData("chair");
      Vector3f chairBack3Trans = new Vector3f(chairSegment*4, 0f, 0f);
      Transform3D tfChairBack3 = new Transform3D();
      tfChairBack3.setTranslation(chairBack3Trans);		
      TransformGroup tgChairBack3 = new TransformGroup(tfChairBack3);
      tgChairBack3.addChild(chairBack3);			

      Box chairBack4 = new Box(chairBackWidth/2, chairBackHeight/2, chairBackWidth/2, Primitive.GENERATE_NORMALS + Primitive.GENERATE_TEXTURE_COORDS, chairApp);
      chairBack4.setUserData("chair");
      Vector3f chairBack4Trans = new Vector3f(chairSegment*6, 0f, 0f);
      Transform3D tfChairBack4 = new Transform3D();
      tfChairBack4.setTranslation(chairBack4Trans);		
      TransformGroup tgChairBack4 = new TransformGroup(tfChairBack4);
      tgChairBack4.addChild(chairBack4);			

      Box chairBack5 = new Box(chairBackWidth/2, chairBackHeight/2, chairBackWidth/2, Primitive.GENERATE_NORMALS + Primitive.GENERATE_TEXTURE_COORDS, chairApp);
      chairBack5.setUserData("chair");
      Vector3f chairBack5Trans = new Vector3f(chairWidth-chairBackWidth, 0f, 0f);
      Transform3D tfChairBack5 = new Transform3D();
      tfChairBack5.setTranslation(chairBack5Trans);		
      TransformGroup tgChairBack5 = new TransformGroup(tfChairBack5);
      tgChairBack5.addChild(chairBack5);					
		
      Box chairHeadrest = new Box(chairWidth/2, chairHeadrestHeight/2, chairBackWidth/2, Primitive.GENERATE_NORMALS + Primitive.GENERATE_TEXTURE_COORDS, chairApp);
      chairHeadrest.setUserData("chair");
      Vector3f chairHeadrestTrans = new Vector3f((chairWidth - chairBackWidth)/2, chairBackHeight-chairHeadrestHeight, 0f);
      Transform3D tfChairHeadrest = new Transform3D();
      tfChairHeadrest.setTranslation(chairHeadrestTrans);		
      TransformGroup tgChairHeadrest = new TransformGroup(tfChairHeadrest);
      tgChairHeadrest.addChild(chairHeadrest);						
		
      Vector3f chairBackTrans = new Vector3f(-(chairWidth - chairBackWidth)/2, chairLegHeight + chairSeatHeight + chairBackHeight/2 -chairBackWidth/2, chairBackWidth/2-.012f);
      Transform3D tfChairBack = new Transform3D();
      tfChairBack.setTranslation(chairBackTrans);		

      //slanted back
      Transform3D chairBackTempTrans = new Transform3D();	  	
      chairBackTempTrans.rotX(-Math.PI/45);	 
      
      tfChairBack.mul(chairBackTempTrans);
	
      TransformGroup tgChairBack = new TransformGroup(tfChairBack);
		
      tgChairBack.addChild(tgChairBack1);				
      tgChairBack.addChild(tgChairBack2);				
      tgChairBack.addChild(tgChairBack3);				
      tgChairBack.addChild(tgChairBack4);				
      tgChairBack.addChild(tgChairBack5);				
      tgChairBack.addChild(tgChairHeadrest);		
      
      /*******************************************************************
      * 
      * A 'HITBOX' - INTENDED TO HELP WITH PICKING
      * 
      ******************************************************************/

      
      Appearance invisibApp = new Appearance();
      TransparencyAttributes fullyTransparent = new TransparencyAttributes();
      fullyTransparent.setTransparencyMode (TransparencyAttributes.NICEST);
      fullyTransparent.setTransparency(1f);
      invisibApp.setTransparencyAttributes(fullyTransparent);
      Box pickingHelper = new Box(.24f,1f,.26f, invisibApp);
      pickingHelper.setUserData("chair");
      Vector3f invisTrans = new Vector3f(0f, 0f, .22f);
      Transform3D tfInvis = new Transform3D();
      tfInvis.setTranslation(invisTrans);
      TransformGroup tgInvis = new TransformGroup(tfInvis);
      tgInvis.addChild(pickingHelper);
		
      /*******************************************************************
      * 
      * PUTTING THE CHAIR PARTS TOGETHER
      * 
      ******************************************************************/

      
      //set up the chair in the scene
      Vector3f chairTrans = new Vector3f(-.6f, 0f, 1.5f);
	  	
      //rotate the entire chair to face the TV at an angle
      Transform3D chairTemp = new Transform3D();
      chairTemp.rotY(Math.PI/1.3f);	  	
      
      //combine the rotation and translation      
      tfChair = new Transform3D();
      tfChair.mul(chairTemp);	  	
      tfChair.setTranslation(chairTrans);		
      
      chair = new TransformGroup(tfChair);
      chair.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
      chair.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
      chair.addChild(tgChairLegs);
      chair.addChild(tgChairSeat);
      chair.addChild(tgChairBack);
      chair.addChild(tgInvis);
      
      theScene.addChild(chair);
   }
	
   //create the floor under the scene - cylindrical because the background will be a sphere
   public void createFloor(){
      float floorRadius = 6f;
      float floorHeight = .0001f;
		
      Vector3f floorTrans = new Vector3f(0f, -floorHeight, 0f);
		
      //gray floor      
      Color3f black = new Color3f(0.0f, 0.0f, 0.0f);
      Color3f floorCol = new Color3f(.54f, .49f, .42f);
      
      Appearance floorApp = new Appearance();
      Material mat = new Material(floorCol, black, floorCol, black, 70f);
      floorApp.setMaterial(mat); 
		
      floor = new Cylinder(floorRadius, floorHeight, floorApp);	
      floor.setUserData("floor");
		
      Transform3D tfFloor = new Transform3D();	  	
      tfFloor.setTranslation(floorTrans);				
      TransformGroup tgFloor = new TransformGroup(tfFloor);
      
      tgFloor.addChild(floor);		

      theScene.addChild(tgFloor);
   }
	
   //enclose the scene in a sphere, paint a background over it
   public void createBackground(){
      float radius = 6f;
                
      TextureLoader bgTextureLoader = new TextureLoader("mars_360.jpg", null);		
      Texture backgroundTexture = bgTextureLoader.getTexture();
			
      Background bg = new Background();
      BoundingSphere bounds = new BoundingSphere(new Point3d(0,0,0),Double.MAX_VALUE);
      bg.setApplicationBounds(bounds);
		
      BranchGroup bgBackground = new BranchGroup();
      Appearance bgBackgroundApp = new Appearance();
      bgBackgroundApp.setTexture(backgroundTexture);		
	
      Sphere background = new Sphere(radius, Sphere.GENERATE_NORMALS + Sphere.GENERATE_NORMALS_INWARD + Sphere.GENERATE_TEXTURE_COORDS, 200, bgBackgroundApp);

      bgBackground.addChild(background);
      bg.setGeometry(bgBackground);
		
      //rotate by 180 to change the visible background image
      Transform3D backgroundTrans = new Transform3D();	  	
      backgroundTrans.rotX(Math.PI);
      TransformGroup tgTransform = new TransformGroup(backgroundTrans);
      tgTransform.addChild(bgBackground);
		
      theScene.addChild(tgTransform);
	}	
	
   public void addLight(){
      //Scene bakground light - directional light
      BranchGroup bgSceneLight = new BranchGroup();
		
      BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0);
      Color3f white = new Color3f(1.0f, 1.0f, 1.0f);
      Vector3f lightDir = new Vector3f(-1.0f,-1.0f,1.0f);
      DirectionalLight dirLight = new DirectionalLight(white, lightDir);
      dirLight.setInfluencingBounds(bounds);
		
      bgSceneLight.addChild(dirLight);
		
      //Point light generated by the lamp
      Point3f lampCentre = new Point3f(0f, 1.1f,0f);
      Point3f lampAttenuation = new Point3f(0f, 0.8f,0.2f);
      lampLight = new PointLight(white, lampCentre, lampAttenuation);
      lampLight.setCapability(PointLight.ALLOW_INFLUENCING_BOUNDS_WRITE);
      lampLight.setInfluencingBounds(bounds);
		
      bgSceneLight.addChild(lampLight);		
		
      universe.addBranchGraph(bgSceneLight);
   }
	
}