package tecrys.svc.trylobot;
import com.fs.starfarer.api.Global;
import tecrys.svc.math.FastTrig;
import java.util.Random;
import org.lwjgl.util.vector.Vector2f;
// original authors:
//   JeffK
//   LazyWizard
// additional contributions/modifications by:
//   Trylobot

public class _
{
	private static final Random rand = new Random();
	
	public static Vector2f translate_polar( Vector2f center, float radius, float angle )
    {
		float radians = (float)Math.toRadians( angle );
        return new Vector2f(
		  (float) FastTrig.cos(radians) * radius + (center == null ? 0f : center.x),
          (float) FastTrig.sin(radians) * radius + (center == null ? 0f : center.y)
		);
    }	

    public static float get_angle( Vector2f vector )
    {
        return (float)Math.toDegrees( Math.atan2( vector.y, vector.x ));
    }

    public static float get_angle( Vector2f from, Vector2f to )
    {
        return get_angle( new Vector2f(
		  to.x - from.x,
		  to.y - from.y ));
    }
	
	public static float get_distance( Vector2f A, Vector2f B )
	{
		Vector2f result = new Vector2f();
		Vector2f.sub( B, A, result );
		return result.length();
	}
	
	public static float get_distance_squared( Vector2f A, Vector2f B )
	{
		Vector2f result = new Vector2f( B.x - A.x, B.y - A.y );
		return result.lengthSquared();
	}
	
	public static float get_random( float low, float high )
	{
		return rand.nextFloat() * (high - low) + low;
	}
	
	public static boolean can_be_loaded( String fullyQualifiedClassName )
	{
		try
		{
			 Global.getSettings().getScriptClassLoader().loadClass( fullyQualifiedClassName );
			 return true;
		}
		catch (ClassNotFoundException ex)
		{
			return false;
		}		
	}
	
	public static void debug( String message )
	{
		if( Global.getSettings().isDevMode() )
		{
			Class caller_class = sun.reflect.Reflection.getCallerClass( 2 );
			
		}
	}
	
	public static void print( String message )
	{
		Class caller_class = sun.reflect.Reflection.getCallerClass( 2 );
		
	}
}
