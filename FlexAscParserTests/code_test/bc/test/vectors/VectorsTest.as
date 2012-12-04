package bc.test.vectors
{
    public class VectorsTest
    {
        public function testVectorCreation() : void
        {
            var v : Vector.<String> = new Vector.<String>();
            v = new<String>[];
            v = new<String>["a", "b", "c"];
            v = Vector.<String>(["a", "b", "c"]);
        }
        
        public function testVectorPush() : void
        {
            var v : Vector.<String> = new<String>[];
            v.push();
            v.push("a");
            v.push("a", "b", "c");
            v.push(["a", "b", "c"]);
        }
        
        public function testVectorIndexer() : void
        {
            var v : Vector.<String> = new<String>[];
            v[0] = "a";
            var val : String = v[0];
        }
        
        public function testVectorPop() : void
        {
            var v:Vector.<String> = new<String>["a"];
            var element:String = v.pop();
        }
    }
}