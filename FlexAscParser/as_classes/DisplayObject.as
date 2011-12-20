public class DisplayObject extends Object
{	
	public function get alpha() : Number;

	public function set alpha(value : Number) : void;

	public function get blendMode() : String;

	public function set blendMode(value : String) : void;

	public function get cacheAsBitmap() : Boolean;

	public function set cacheAsBitmap(value : Boolean) : void;

	public function get filters() : Array;

	public function set filters(value : Array) : void;

	public function getBounds(targetCoordinateSpace : DisplayObject) : Rectangle;

	public function getRect(targetCoordinateSpace : DisplayObject) : Rectangle;

	public function globalToLocal(point : Point) : Point;

	public function get height() : Number;

	public function set height(value : Number) : void;

	public function hitTestObject(obj : DisplayObject) : Boolean;

	public function hitTestPoint(x : Number, y : Number) : Boolean;

	public function hitTestPoint(x : Number, y : Number, shapeFlag : Boolean) : Boolean;

	public function localToGlobal(point : Point) : Point;

	public function get mask() : DisplayObject;

	public function set mask(value : DisplayObject) : void;

	public function get mouseX() : Number;

	public function get mouseY() : Number;

	public function get name() : String;

	public function set name(value : String) : void;

	public function get opaqueBackground() : Object;

	public function set opaqueBackground(value : Object) : void;

	public function get parent() : DisplayObjectContainer;

	public function get root() : DisplayObject;

	public function get rotation() : Number;

	public function set rotation(value : Number) : void;

	public function get rotationX() : Number;

	public function set rotationX(value : Number) : void;

	public function get rotationY() : Number;

	public function set rotationY(value : Number) : void;

	public function get rotationZ() : Number;

	public function set rotationZ(value : Number) : void;

	public function get scale9Grid() : Rectangle;

	public function set scale9Grid(innerRectangle : Rectangle) : void;

	public function get scaleX() : Number;

	public function set scaleX(value : Number) : void;

	public function get scaleY() : Number;

	public function set scaleY(value : Number) : void;

	public function get scaleZ() : Number;

	public function set scaleZ(value : Number) : void;

	public function get scrollRect() : Rectangle;

	public function set scrollRect(value : Rectangle) : void;

	public function get stage() : Stage;

	public function get transform() : Transform;

	public function set transform(value : Transform) : void;

	public function get visible() : Boolean;

	public function set visible(value : Boolean) : void;

	public function get width() : Number;

	public function set width(value : Number) : void;

	public function get x() : Number;

	public function set x(value : Number) : void;

	public function get y() : Number;

	public function set y(value : Number) : void;	
}