package schemacrawler.tools.text.utility.org.json;


/*
 Copyright (c) 2002 JSON.org

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 The Software shall be used for Good, not Evil.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @see <a href=
 *      "https://github.com/stleary/JSON-java">stleary/JSON-java</a>
 * @author JSON.org
 * @version 2011-04-05
 */
public class JSONObject
{

  /**
   * JSONObject.NULL is equivalent to the value that JavaScript calls
   * null, whilst Java's null is equivalent to the value that JavaScript
   * calls undefined.
   */
  private static final class Null
  {

    /**
     * A Null object is equal to the null value and to itself.
     *
     * @param object
     *        An object to test for nullness.
     * @return true if the object parameter is the JSONObject.NULL
     *         object or null.
     */
    @Override
    public boolean equals(final Object object)
    {
      return object == null || object == this;
    }

    /**
     * Get the "null" string value.
     *
     * @return The string "null".
     */
    @Override
    public String toString()
    {
      return "null";
    }

    /**
     * There is only intended to be a single instance of the NULL
     * object, so the clone method returns itself.
     *
     * @return NULL.
     */
    @Override
    protected final Object clone()
    {
      return this;
    }
  }

  /**
   * It is sometimes more convenient and less ambiguous to have a
   * <code>NULL</code> object than to use Java's <code>null</code>
   * value. <code>JSONObject.NULL.equals(null)</code> returns
   * <code>true</code>. <code>JSONObject.NULL.toString()</code> returns
   * <code>"null"</code>.
   */
  private static final Object NULL = new Null();

  /**
   * Throw an exception if the object is a NaN or infinite number.
   *
   * @param o
   *        The object to test.
   * @throws JSONException
   *         If o is a non-finite number.
   */
  public static void testValidity(final Object o)
    throws JSONException
  {
    if (o != null)
    {
      if (o instanceof Double)
      {
        if (((Double) o).isInfinite() || ((Double) o).isNaN())
        {
          throw new JSONException("JSON does not allow non-finite numbers.");
        }
      }
      else if (o instanceof Float)
      {
        if (((Float) o).isInfinite() || ((Float) o).isNaN())
        {
          throw new JSONException("JSON does not allow non-finite numbers.");
        }
      }
    }
  }

  /**
   * Make a JSON text of an Object value. If the object has an
   * value.toJSONString() method, then that method will be used to
   * produce the JSON text. The method is required to produce a strictly
   * conforming text. If the object does not contain a toJSONString
   * method (which is the most common case), then a text will be
   * produced by other means. If the value is an array or Collection,
   * then a JSONArray will be made from it and its toJSONString method
   * will be called. If the value is a MAP, then a JSONObject will be
   * made from it and its toJSONString method will be called. Otherwise,
   * the value's toString method will be called, and the result will be
   * quoted.
   * <p>
   * Warning: This method assumes that the data structure is acyclical.
   *
   * @param value
   *        The value to be serialized.
   * @return a printable, displayable, transmittable representation of
   *         the object, beginning with <code>{</code>&nbsp;<small>(left
   *         brace)</small> and ending with <code>}</code> &nbsp;
   *         <small>(right brace)</small>.
   * @throws JSONException
   *         If the value is or contains an invalid number.
   */
  static String valueToString(final Object value)
    throws JSONException
  {
    if (value == null || value.equals(null))
    {
      return "null";
    }
    if (value instanceof Number)
    {
      return numberToString((Number) value);
    }
    if (value instanceof Boolean || value instanceof JSONObject
        || value instanceof JSONArray)
    {
      return value.toString();
    }
    if (value instanceof Map)
    {
      return new JSONObject(value).toString();
    }
    if (value instanceof Collection)
    {
      return new JSONArray((Collection) value).toString();
    }
    if (value.getClass().isArray())
    {
      return new JSONArray(value).toString();
    }
    return quote(value.toString());
  }

  /**
   * Make a prettyprinted JSON text of an object value.
   * <p>
   * Warning: This method assumes that the data structure is acyclical.
   *
   * @param value
   *        The value to be serialized.
   * @param indentFactor
   *        The number of spaces to add to each level of indentation.
   * @param indent
   *        The indentation of the top level.
   * @return a printable, displayable, transmittable representation of
   *         the object, beginning with <code>{</code>&nbsp;<small>(left
   *         brace)</small> and ending with <code>}</code> &nbsp;
   *         <small>(right brace)</small>.
   * @throws JSONException
   *         If the object contains an invalid number.
   */
  static String valueToString(final Object value,
                              final int indentFactor,
                              final int indent)
    throws JSONException
  {
    if (value == null || value.equals(null))
    {
      return "null";
    }
    if (value instanceof Number)
    {
      return numberToString((Number) value);
    }
    if (value instanceof Boolean)
    {
      return value.toString();
    }
    if (value instanceof JSONObject)
    {
      return ((JSONObject) value).toString(indentFactor, indent);
    }
    if (value instanceof JSONArray)
    {
      return ((JSONArray) value).toString(indentFactor, indent);
    }
    if (value instanceof Map)
    {
      return new JSONObject(value).toString(indentFactor, indent);
    }
    if (value instanceof Collection)
    {
      return new JSONArray((Collection) value).toString(indentFactor, indent);
    }
    if (value.getClass().isArray())
    {
      return new JSONArray(value).toString(indentFactor, indent);
    }
    return quote(value.toString());
  }

  /**
   * Wrap an object, if necessary. If the object is null, return the
   * NULL object. If it is an array or collection, wrap it in a
   * JSONArray. If it is a map, wrap it in a JSONObject. If it is a
   * standard property (Double, String, et al) then it is already
   * wrapped. Otherwise, if it comes from one of the java packages, turn
   * it into a string. And if it doesn't, try to wrap it in a
   * JSONObject. If the wrapping fails, then null is returned.
   *
   * @param object
   *        The object to wrap
   * @return The wrapped value
   */
  static Object wrap(final Object object)
  {
    try
    {
      if (object == null)
      {
        return NULL;
      }
      if (object instanceof JSONObject || object instanceof JSONArray
          || NULL.equals(object) || object instanceof Byte
          || object instanceof Character || object instanceof Short
          || object instanceof Integer || object instanceof Long
          || object instanceof Boolean || object instanceof Float
          || object instanceof Double || object instanceof String)
      {
        return object;
      }

      if (object instanceof Collection)
      {
        return new JSONArray((Collection) object);
      }
      if (object.getClass().isArray())
      {
        return new JSONArray(object);
      }
      if (object instanceof Map)
      {
        return new JSONObject(object);
      }
      final Package objectPackage = object.getClass().getPackage();
      final String objectPackageName = objectPackage != null? objectPackage
        .getName(): "";
      if (objectPackageName.startsWith("java.")
          || objectPackageName.startsWith("javax.")
          || object.getClass().getClassLoader() == null)
      {
        return object.toString();
      }
      return new JSONObject(object);
    }
    catch (final Exception exception)
    {
      return null;
    }
  }

  /**
   * Produce a string from a Number.
   *
   * @param number
   *        A Number
   * @return A String.
   * @throws JSONException
   *         If n is a non-finite number.
   */
  private static String numberToString(final Number number)
    throws JSONException
  {
    if (number == null)
    {
      throw new JSONException("Null pointer");
    }
    testValidity(number);

    // Shave off trailing zeros and decimal point, if possible.

    String string = number.toString();
    if (string.indexOf('.') > 0 && string.indexOf('e') < 0
        && string.indexOf('E') < 0)
    {
      while (string.endsWith("0"))
      {
        string = string.substring(0, string.length() - 1);
      }
      if (string.endsWith("."))
      {
        string = string.substring(0, string.length() - 1);
      }
    }
    return string;
  }

  /**
   * Produce a string in double quotes with backslash sequences in all
   * the right places. A backslash will be inserted within </, producing
   * <\/, allowing JSON text to be delivered in HTML. In JSON text, a
   * string cannot contain a control character or an unescaped quote or
   * backslash.
   *
   * @param string
   *        A String
   * @return A String correctly formatted for insertion in a JSON text.
   */
  private static String quote(final String string)
  {
    if (string == null || string.length() == 0)
    {
      return "\"\"";
    }

    char b;
    char c = 0;
    String hhhh;
    int i;
    final int len = string.length();
    final StringBuffer sb = new StringBuffer(len + 4);

    sb.append('"');
    for (i = 0; i < len; i += 1)
    {
      b = c;
      c = string.charAt(i);
      switch (c)
      {
        case '\\':
        case '"':
          sb.append('\\');
          sb.append(c);
          break;
        case '/':
          if (b == '<')
          {
            sb.append('\\');
          }
          sb.append(c);
          break;
        case '\b':
          sb.append("\\b");
          break;
        case '\t':
          sb.append("\\t");
          break;
        case '\n':
          sb.append("\\n");
          break;
        case '\f':
          sb.append("\\f");
          break;
        case '\r':
          sb.append("\\r");
          break;
        default:
          if (c < ' ' || c >= '\u0080' && c < '\u00a0'
              || c >= '\u2000' && c < '\u2100')
          {
            hhhh = "000" + Integer.toHexString(c);
            sb.append("\\u" + hhhh.substring(hhhh.length() - 4));
          }
          else
          {
            sb.append(c);
          }
      }
    }
    sb.append('"');
    return sb.toString();
  }

  /**
   * The map where the JSONObject's properties are kept.
   */
  private final Map map;

  /**
   * Construct an empty JSONObject.
   */
  public JSONObject()
  {
    map = new HashMap();
  }

  /**
   * Construct a JSONObject from an Object using bean getters. It
   * reflects on all of the public methods of the object. For each of
   * the methods with no parameters and a name starting with
   * <code>"get"</code> or <code>"is"</code> followed by an uppercase
   * letter, the method is invoked, and a key and the value returned
   * from the getter method are put into the new JSONObject. The key is
   * formed by removing the <code>"get"</code> or <code>"is"</code>
   * prefix. If the second remaining character is not upper case, then
   * the first character is converted to lower case. For example, if an
   * object has a method named <code>"getName"</code>, and if the result
   * of calling <code>object.getName()</code> is
   * <code>"Larry Fine"</code>, then the JSONObject will contain
   * <code>"name": "Larry Fine"</code>.
   *
   * @param bean
   *        An object that has getter methods that should be used to
   *        make a JSONObject.
   */
  private JSONObject(final Object bean)
  {
    this();
    populateMap(bean);
  }

  /**
   * Accumulate values under a key. It is similar to the put method
   * except that if there is already an object stored under the key then
   * a JSONArray is stored under the key to hold all of the accumulated
   * values. If there is already a JSONArray, then the new value is
   * appended to it. In contrast, the put method replaces the previous
   * value. If only one value is accumulated that is not a JSONArray,
   * then the result will be the same as using put. But if multiple
   * values are accumulated, then the result will be like append.
   *
   * @param key
   *        A key string.
   * @param value
   *        An object to be accumulated under the key.
   * @return this.
   * @throws JSONException
   *         If the value is an invalid number or if the key is null.
   */
  public JSONObject accumulate(final String key, final Object value)
    throws JSONException
  {
    testValidity(value);
    final Object object = opt(key);
    if (object == null)
    {
      put(key, value instanceof JSONArray? new JSONArray().put(value): value);
    }
    else if (object instanceof JSONArray)
    {
      ((JSONArray) object).put(value);
    }
    else
    {
      put(key, new JSONArray().put(object).put(value));
    }
    return this;
  }

  /**
   * Get the number of keys stored in the JSONObject.
   *
   * @return The number of keys in the JSONObject.
   */
  public int length()
  {
    return map.size();
  }

  /**
   * Put a key/boolean pair in the JSONObject.
   *
   * @param key
   *        A key string.
   * @param value
   *        A boolean which is the value.
   * @return this.
   * @throws JSONException
   *         If the key is null.
   */
  public JSONObject put(final String key, final boolean value)
    throws JSONException
  {
    put(key, value? Boolean.TRUE: Boolean.FALSE);
    return this;
  }

  /**
   * Put a key/value pair in the JSONObject, where the value will be a
   * JSONArray which is produced from a Collection.
   *
   * @param key
   *        A key string.
   * @param value
   *        A Collection value.
   * @return this.
   * @throws JSONException
   */
  public JSONObject put(final String key, final Collection value)
    throws JSONException
  {
    put(key, new JSONArray(value));
    return this;
  }

  /**
   * Put a key/double pair in the JSONObject.
   *
   * @param key
   *        A key string.
   * @param value
   *        A double which is the value.
   * @return this.
   * @throws JSONException
   *         If the key is null or if the number is invalid.
   */
  public JSONObject put(final String key, final double value)
    throws JSONException
  {
    put(key, new Double(value));
    return this;
  }

  /**
   * Put a key/int pair in the JSONObject.
   *
   * @param key
   *        A key string.
   * @param value
   *        An int which is the value.
   * @return this.
   * @throws JSONException
   *         If the key is null.
   */
  public JSONObject put(final String key, final int value)
    throws JSONException
  {
    put(key, new Integer(value));
    return this;
  }

  /**
   * Put a key/long pair in the JSONObject.
   *
   * @param key
   *        A key string.
   * @param value
   *        A long which is the value.
   * @return this.
   * @throws JSONException
   *         If the key is null.
   */
  public JSONObject put(final String key, final long value)
    throws JSONException
  {
    put(key, new Long(value));
    return this;
  }

  /**
   * Put a key/value pair in the JSONObject. If the value is null, then
   * the key will be removed from the JSONObject if it is present.
   *
   * @param key
   *        A key string.
   * @param value
   *        An object which is the value. It should be of one of these
   *        types: Boolean, Double, Integer, JSONArray, JSONObject,
   *        Long, String, or the JSONObject.NULL object.
   * @return this.
   * @throws JSONException
   *         If the value is non-finite number or if the key is null.
   */
  public JSONObject put(final String key, final Object value)
    throws JSONException
  {
    if (key == null)
    {
      throw new JSONException("Null key.");
    }
    if (value != null)
    {
      testValidity(value);
      map.put(key, value);
    }
    else
    {
      remove(key);
    }
    return this;
  }

  /**
   * Make a JSON text of this JSONObject. For compactness, no whitespace
   * is added. If this would not result in a syntactically correct JSON
   * text, then null will be returned instead.
   * <p>
   * Warning: This method assumes that the data structure is acyclical.
   *
   * @return a printable, displayable, portable, transmittable
   *         representation of the object, beginning with <code>{</code>
   *         &nbsp;<small>(left brace)</small> and ending with
   *         <code>}</code>&nbsp;<small>(right brace)</small>.
   */
  @Override
  public String toString()
  {
    try
    {
      final Iterator keys = keys();
      final StringBuffer sb = new StringBuffer("{");

      while (keys.hasNext())
      {
        if (sb.length() > 1)
        {
          sb.append(',');
        }
        final Object o = keys.next();
        sb.append(quote(o.toString()));
        sb.append(':');
        sb.append(valueToString(map.get(o)));
      }
      sb.append('}');
      return sb.toString();
    }
    catch (final Exception e)
    {
      return null;
    }
  }

  public void write(final Writer writer, final int indentFactor)
    throws JSONException
  {
    write(new PrintWriter(writer), indentFactor, 0);
  }

  /**
   * Write the contents of the JSONObject as JSON text to a writer. For
   * compactness, no whitespace is added.
   * <p>
   * Warning: This method assumes that the data structure is acyclical.
   *
   * @return The writer.
   * @throws JSONException
   */
  Writer write(final Writer writer)
    throws JSONException
  {
    try
    {
      boolean commanate = false;
      final Iterator keys = keys();
      writer.write('{');

      while (keys.hasNext())
      {
        if (commanate)
        {
          writer.write(',');
        }
        final Object key = keys.next();
        writer.write(quote(key.toString()));
        writer.write(':');
        final Object value = map.get(key);
        if (value instanceof JSONObject)
        {
          ((JSONObject) value).write(writer);
        }
        else if (value instanceof JSONArray)
        {
          ((JSONArray) value).write(writer);
        }
        else
        {
          writer.write(valueToString(value));
        }
        commanate = true;
      }
      writer.write('}');
      return writer;
    }
    catch (final IOException exception)
    {
      throw new JSONException(exception);
    }
  }

  /**
   * Get an enumeration of the keys of the JSONObject.
   *
   * @return An iterator of the keys.
   */
  private Iterator keys()
  {
    return map.keySet().iterator();
  }

  /**
   * Get an optional value associated with a key.
   *
   * @param key
   *        A key string.
   * @return An object which is the value, or null if there is no value.
   */
  private Object opt(final String key)
  {
    return key == null? null: map.get(key);
  }

  private void populateMap(final Object bean)
  {
    final Class klass = bean.getClass();

    // If klass is a System class then set includeSuperClass to false.

    final boolean includeSuperClass = klass.getClassLoader() != null;

    final Method[] methods = includeSuperClass? klass.getMethods()
                                              : klass.getDeclaredMethods();
    for (final Method method: methods)
    {
      try
      {
        if (Modifier.isPublic(method.getModifiers()))
        {
          final String name = method.getName();
          String key = "";
          if (name.startsWith("get"))
          {
            if (name.equals("getClass") || name.equals("getDeclaringClass"))
            {
              key = "";
            }
            else
            {
              key = name.substring(3);
            }
          }
          else if (name.startsWith("is"))
          {
            key = name.substring(2);
          }
          if (key.length() > 0 && Character.isUpperCase(key.charAt(0))
              && method.getParameterTypes().length == 0)
          {
            if (key.length() == 1)
            {
              key = key.toLowerCase();
            }
            else if (!Character.isUpperCase(key.charAt(1)))
            {
              key = key.substring(0, 1).toLowerCase() + key.substring(1);
            }

            final Object result = method.invoke(bean, (Object[]) null);
            if (result != null)
            {
              map.put(key, wrap(result));
            }
          }
        }
      }
      catch (final Exception ignore)
      {
      }
    }
  }

  /**
   * Remove a name and its value, if present.
   *
   * @param key
   *        The name to be removed.
   * @return The value that was associated with the name, or null if
   *         there was no value.
   */
  private Object remove(final String key)
  {
    return map.remove(key);
  }

  /**
   * Make a prettyprinted JSON text of this JSONObject.
   * <p>
   * Warning: This method assumes that the data structure is acyclical.
   *
   * @param indentFactor
   *        The number of spaces to add to each level of indentation.
   * @param indent
   *        The indentation of the top level.
   * @return a printable, displayable, transmittable representation of
   *         the object, beginning with <code>{</code>&nbsp;<small>(left
   *         brace)</small> and ending with <code>}</code> &nbsp;
   *         <small>(right brace)</small>.
   * @throws JSONException
   *         If the object contains an invalid number.
   */
  private String toString(final int indentFactor, final int indent)
    throws JSONException
  {
    int i;
    final int length = length();
    if (length == 0)
    {
      return "{}";
    }
    final Iterator keys = keys();
    final int newindent = indent + indentFactor;
    Object object;
    final StringBuffer sb = new StringBuffer("{");
    if (length == 1)
    {
      object = keys.next();
      sb.append(quote(object.toString()));
      sb.append(": ");
      sb.append(valueToString(map.get(object), indentFactor, indent));
    }
    else
    {
      while (keys.hasNext())
      {
        object = keys.next();
        if (sb.length() > 1)
        {
          sb.append(",\n");
        }
        else
        {
          sb.append('\n');
        }
        for (i = 0; i < newindent; i += 1)
        {
          sb.append(' ');
        }
        sb.append(quote(object.toString()));
        sb.append(": ");
        sb.append(valueToString(map.get(object), indentFactor, newindent));
      }
      if (sb.length() > 1)
      {
        sb.append('\n');
        for (i = 0; i < indent; i += 1)
        {
          sb.append(' ');
        }
      }
    }
    sb.append('}');
    return sb.toString();
  }

  /**
   * Make a prettyprinted JSON text of this JSONObject.
   * <p>
   * Warning: This method assumes that the data structure is acyclical.
   *
   * @param indentFactor
   *        The number of spaces to add to each level of indentation.
   * @param indent
   *        The indentation of the top level.
   * @return a printable, displayable, transmittable representation of
   *         the object, beginning with <code>{</code>&nbsp;<small>(left
   *         brace)</small> and ending with <code>}</code> &nbsp;
   *         <small>(right brace)</small>.
   * @throws JSONException
   *         If the object contains an invalid number.
   */
  private void write(final PrintWriter writer,
                     final int indentFactor,
                     final int indent)
    throws JSONException
  {
    int i;
    final int length = length();
    if (length == 0)
    {
      writer.println("{}");
      return;
    }
    final Iterator keys = keys();
    final int newindent = indent + indentFactor;
    Object object;
    writer.print("{");
    if (length == 1)
    {
      object = keys.next();
      writer.print(quote(object.toString()));
      writer.print(": ");
      final Object value = map.get(object);
      if (value instanceof JSONObject)
      {
        ((JSONObject) value).write(writer, indentFactor, newindent);
      }
      else if (value instanceof JSONArray)
      {
        ((JSONArray) value).write(writer, indentFactor, newindent);
      }
      else
      {
        writer.println(valueToString(value));
      }
    }
    else
    {
      int keyCount = 0;
      while (keys.hasNext())
      {
        keyCount++;
        object = keys.next();
        if (keyCount > 1)
        {
          writer.println(",");
        }
        else
        {
          writer.println();
        }
        for (i = 0; i < newindent; i += 1)
        {
          writer.print(' ');
        }
        writer.print(quote(object.toString()));
        writer.print(": ");
        final Object value = map.get(object);
        if (value instanceof JSONObject)
        {
          ((JSONObject) value).write(writer, indentFactor, newindent);
        }
        else if (value instanceof JSONArray)
        {
          ((JSONArray) value).write(writer, indentFactor, newindent);
        }
        else
        {
          writer.print(valueToString(value));
        }
      }
      if (keyCount > 1)
      {
        writer.println();
        for (i = 0; i < indent; i += 1)
        {
          writer.print(' ');
        }
      }
    }
    writer.print('}');
  }

}
