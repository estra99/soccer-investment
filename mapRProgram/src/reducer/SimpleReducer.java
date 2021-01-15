package reducer;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import java.io.IOException;
import java.util.Arrays;

public class SimpleReducer extends Reducer<Text, Text, Text, Text> {

    public void reduce(Text key, Iterable<Text> player_details, Context context) throws IOException, InterruptedException {

        if(key.toString().equals("France")){
            for( Text arr: player_details){
                String[] temp = arr.toString().split(",");
                context.write(new Text(temp[0]), new Text(String.join("\t",Arrays.copyOfRange(temp, 1, 6))));
            }
        }
    }
}
