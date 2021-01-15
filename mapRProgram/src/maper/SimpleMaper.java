package maper;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.IOException;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class SimpleMaper extends Mapper<Object, Text, Text, Text> {

   public void map(Object key, Text value, Context context) throws IOException
   {
	   try
	   {
		   BufferedReader TSVFile = new BufferedReader(new StringReader(value.toString()));
		   String dataRow = TSVFile.readLine(); // Read first line.

		   while (dataRow != null){

		   	  String[] playerValues = dataRow.split("\t");
		   	  Text nationality = new Text(playerValues[26]);
		   	  String playerValuesFiltered = playerValues[1] + ',' + playerValues[2] + ',' + playerValues[5] + ',' +  playerValues[11] + ',' + playerValues[28] + ',' + playerValues[31];
		   	  Text playerValuesFilteredText = new Text (playerValuesFiltered);
		   	  context.write(nationality, playerValuesFilteredText);
		   	  dataRow = TSVFile.readLine(); // Read next line of data.
		   }
		   // Close the file once all data has been read.
		   TSVFile.close();

	   }
	   catch (Exception ex)
	   {
		   System.out.println(ex.getMessage());
	   }


   }
}






