package maper;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.Reader;
import java.io.IOException;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class SimpleMaper extends Mapper<Object, Text, Text, Text> {

   public void map(Object key, Text value, Context context) throws IOException
   {
	   Reader inputString = new StringReader(value.toString());
   	   BufferedReader br = new BufferedReader(inputString);
	   String line = br.readLine(); // obtiene el encabezado, para que entre en el while con la primera línea de datos.

	   while ((line = br.readLine()) != null)
	   {
		   try
		   {

			   String[] values = line.split(",");
			   Text nationality = new Text(values[27]);
			   String playerValues = values[1] + ',' + values[2] + ',' + values[5] + ',' +  values[11] + ',' + values[29] + ',' + values[32];
			   Text playerValuesText = new Text(playerValues);

			   context.write(nationality, playerValuesText);
		   }

		   catch (Exception ex)

		   {
			   System.out.println(ex.getMessage());
		   }
	   }
   }
}
		/*
	   //StringTokenizer itr = new StringTokenizer(value.toString(), ",");
    	 try 
    	 {
    		 String fechastr = itr.nextToken();
			 String monto = itr.nextToken();
    		 System.out.println(monto);
    		 Float amount = Float.parseFloat(monto);
    		 Date fecha = new SimpleDateFormat("MM/dd/yyyy").parse(fechastr);
    		 Text nameofmonth = new Text(new SimpleDateFormat("MMM").format(fecha));
    		 Text year = new Text(new SimpleDateFormat("yyyy").format(fecha));
        	 FloatWritable amountToWrite = new FloatWritable(amount);
			 MapWritable map = new MapWritable();
			 map.put(nameofmonth, amountToWrite);
        	 context.write(year, map);
    	 } 
    	 catch (Exception ex) 
    	 {
    		 System.out.println(ex.getMessage());
    	 }
	    */



