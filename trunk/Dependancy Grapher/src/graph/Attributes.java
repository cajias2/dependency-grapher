package graph;


/**
 * The class Attribute.
 * Has information regarding the attributes of the <dependency> tag
 * @author biggie
 *
 */
public class Attributes
{
	private String _attribute;
	private boolean _isRequired = false;

	//Constructor
	public Attributes(String name_)
	{
		_attribute = name_;
	}
	public Attributes(String name_, boolean isRequired_)
	{
		_attribute = name_;
		_isRequired = isRequired_;			
	}
	//Getters n Setters
	public String getName()
	{
		return _attribute;
	}
	public boolean isRequired()
	{
		return _isRequired;
	}
	
	public String toString()
	{
		if( _isRequired)
		{
			return "**"+_attribute+"**";
		}
		return _attribute;
	}
	
    public boolean equals(Object o)
    {
      boolean compare = false;

      if (o instanceof Attributes)
      {
    	  Attributes attrToCompare = (Attributes)o;
        if (!equals(attrToCompare))
        {
          compare = _attribute.equals(attrToCompare.getName()) && XOR( _isRequired, attrToCompare.isRequired());
        }
      }
      return compare;		
    }
    /*
     * Logical xor operation
     */
    private boolean XOR(boolean a, boolean b)
    {
    	return ( (a&&b) || (!a&&!b) );
    }

}