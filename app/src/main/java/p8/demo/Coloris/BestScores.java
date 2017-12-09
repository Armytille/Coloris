package p8.demo.Coloris;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class BestScores extends ArrayAdapter<User> {

    public BestScores(Context context, ArrayList<User> users) {
        super(context, 0, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        User user = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.user_items, parent,
                    false);
        }
        // Lookup view for data population
        TextView userName = (TextView) convertView.findViewById(R.id.names);
        TextView userScore = (TextView) convertView.findViewById(R.id.scores);
        // Populate the data into the template view using the data object
        userName.setText(user.name);
        userScore.setText(String.valueOf(user.score));
        // Return the completed view to render on screen
        return convertView;
    }
}
