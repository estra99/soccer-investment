package reducer;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import java.io.IOException;
import java.util.Arrays;

public class SimpleReducer extends Reducer<Text, Text[], Text, Text[]> {

    public void reduce(Text key, Iterable<Text[]> player_details, Context context ) throws IOException, InterruptedException {

        if(key.toString().equals("France")){
            for( Text[] arr: player_details){
                context.write(arr[0], Arrays.copyOfRange(arr, 1, 6));
            }
        }


    }
}

/*
      result.clear();

      for (Map mes : values) {
          String monthKey = mes.keySet().toArray()[0].toString();
          float monthValue = Float.parseFloat(mes.values().toArray()[0].toString());
          if (meses.contains(monthKey)) {
              meses.put(monthKey, meses.get(monthKey) + (float) mes.get(monthKey));
          } else {
              meses.put(monthKey, monthValue);
          }
      }

      // sacar el mes con mayor suma
        System.out.println(meses.size());

        String maxEntryId ="";
        float maxValue = 0f;

        for (Map.Entry<String,Float> mes : meses.entrySet()) {
            String monthKey = mes.getKey();
            float entryValue = mes.getValue();
            if( entryValue > maxValue) {
                maxValue = entryValue;
                maxEntryId = monthKey;
            }
        }

        Text monthToSend = new Text(maxEntryId);
        FloatWritable amountToSend = new FloatWritable(maxValue);

        result.put(monthToSend, amountToSend );
        context.write(key, result);

*/