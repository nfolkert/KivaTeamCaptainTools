package com.nfolkert.utils;

import com.nfolkert.json.JSONObject;
import com.nfolkert.json.JSONArray;
import com.nfolkert.json.JSONException;
import com.nfolkert.json.JSONString;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

/**
 */
public class JSONUtils
{
    public static class FormattedPrinter
    {
        final Object _toPrint;
        int _indentFactor = 2;

        int _currentIndent = 0;
        boolean _suppressIndent = false;
        final StringBuilder _buf = new StringBuilder();
        String _result;

        boolean _inlineNonObjectArrayEntries;

        public FormattedPrinter(Object toPrint)
        {
            _toPrint = toPrint;
        }

        public String formatPrint()
        {
            if (_result == null)
            {
                try
                {
                    if (_toPrint instanceof JSONObject)
                        formatPrint((JSONObject) _toPrint);
                    else if (_toPrint instanceof JSONArray)
                        formatPrint((JSONArray) _toPrint);
                    else
                    {
                        _result = _toPrint.toString();
                        return _result;
                    }
                }
                catch (JSONException e)
                {
                    return "JSON FAIL";
                }
                _result = _buf.toString();
            }
            return _result;
        }


        private void formatPrint(JSONArray obj)
                throws JSONException
        {
            int len = obj.length();
            if (!_suppressIndent) for (int j = 0; j < _currentIndent; j ++) { _buf.append(' '); }
            if (len == 0)
            {
                _buf.append("[]");
                return;
            }

            _buf.append("[");
            int originalIndent = _currentIndent;
            _currentIndent = _currentIndent + _indentFactor;

            boolean lastWasObject = false;
            for (int i = 0; i < len; i++)
            {
                final Object entry = obj.get(i);
                if (i > 0) _buf.append(",");
                if (lastWasObject || !_inlineNonObjectArrayEntries || entry instanceof JSONArray || entry instanceof JSONObject)
                {
                    _buf.append("\n");
                    boolean originalSuppress = _suppressIndent;
                    _suppressIndent = false;
                    valueToString(entry);
                    _suppressIndent = originalSuppress;
                    lastWasObject = !_inlineNonObjectArrayEntries || entry instanceof JSONArray || entry instanceof JSONObject;
                }
                else
                {
                    boolean originalSuppress = _suppressIndent;
                    _suppressIndent = true;
                    valueToString(entry);
                    _suppressIndent = originalSuppress;
                    lastWasObject = false;
                }
            }
            _currentIndent = originalIndent;

            if (lastWasObject)
            {
                _buf.append("\n");
                for (int j = 0; j < _currentIndent; j ++) { _buf.append(' '); }
            }
            _buf.append(']');
        }

        private void formatPrint(JSONObject obj)
                throws JSONException
        {
            int i;
            int n = obj.length();

            if (!_suppressIndent) for (int j = 0; j < _currentIndent; j ++) { _buf.append(' '); }

            if (n == 0) {
                _buf.append("{}");
                return;
            }
            List<String> keys = new ArrayList<String>();
            for (Iterator it = obj.keys(); it.hasNext();)
                keys.add((String) it.next());
            Collections.sort(keys);
            _buf.append("{\n");

            int originalIndent = _currentIndent;
            _currentIndent = _currentIndent + _indentFactor;

            boolean isFirst = true;
            for (String key: keys)
            {
                if (isFirst)
                    isFirst = false;
                else
                    _buf.append(",\n");

                for (i = 0; i < _currentIndent; i++)
                    _buf.append(' ');
                JSONObject.quote(key, _buf);
                _buf.append(":");
                boolean originalSuppress = _suppressIndent;
                _suppressIndent = true;
                valueToString(obj.get(key));
                _suppressIndent = originalSuppress;
            }
            _currentIndent = originalIndent;

            _buf.append("\n");
            for (int j = 0; j < _currentIndent; j ++) { _buf.append(' '); }
            _buf.append('}');
        }

        private void valueToString(Object value)
               throws JSONException
        {
           if (value == null )
           {
               if (!_suppressIndent) for (int j = 0; j < _currentIndent; j ++) { _buf.append(' '); }
               _buf.append( "null");
               return;
           }
           try {
               if (value instanceof JSONString) {
                   Object o = ((JSONString)value).toJSONString();
                   if (o instanceof String)
                   {
                       if (!_suppressIndent) for (int j = 0; j < _currentIndent; j ++) { _buf.append(' '); }
                       _buf.append((String)o);
                       return;
                   }
               }
           } catch (Exception e) {
               /* forget about it */
           }

           if (value instanceof Number)
           {
               if (!_suppressIndent) for (int j = 0; j < _currentIndent; j ++) { _buf.append(' '); }
               JSONObject.numberToString((Number) value, _buf);
           }
           else if (value instanceof Boolean)
           {
               if (!_suppressIndent) for (int j = 0; j < _currentIndent; j ++) { _buf.append(' '); }
               _buf.append(value.toString());
           }
           else if (value instanceof JSONObject)
           {
               formatPrint((JSONObject) value);
           }
           else if (value instanceof JSONArray)
           {
               formatPrint((JSONArray) value);
           }
           else if (!(value instanceof String) && value.toString().equals("null"))
           {
               if (!_suppressIndent) for (int j = 0; j < _currentIndent; j ++) { _buf.append(' '); }
               _buf.append( "null");
           }
           else
           {
               if (!_suppressIndent) for (int j = 0; j < _currentIndent; j ++) { _buf.append(' '); }
               JSONObject.quote(value.toString(), _buf);
           }
       }

        public void setInlineNonObjectArrayEntries(boolean inlineNonObjectArrayEntries)
        {
            _inlineNonObjectArrayEntries = inlineNonObjectArrayEntries;
        }
    }
}
