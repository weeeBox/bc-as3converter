public class DisplayObjectContainer extends InteractiveObject
{
	public function addChild(child : DisplayObject) : DisplayObject;

	public function addChildAt(child : DisplayObject, index : int) : DisplayObject;

	public function areInaccessibleObjectsUnderPoint(point : Point) : Boolean;

	public function contains(child : DisplayObject) : Boolean;

	public function getChildAt(index : int) : DisplayObject;

	public function getChildByName(name : String) : DisplayObject;

	public function getChildIndex(child : DisplayObject) : int;

	public function getObjectsUnderPoint(point : Point) : Array;

	public function get mouseChildren() : Boolean;

	public function set mouseChildren(enable : Boolean) : void;

	public function get numChildren() : int;

	public function removeChild(child : DisplayObject) : DisplayObject;

	public function removeChildAt(index : int) : DisplayObject;

	public function setChildIndex(child : DisplayObject, index : int) : void;

	public function swapChildren(child1 : DisplayObject, child2 : DisplayObject) : void;

	public function swapChildrenAt(index1 : int, index2 : int) : void;

	public function get tabChildren() : Boolean;

	public function set tabChildren(enable : Boolean) : void;
}