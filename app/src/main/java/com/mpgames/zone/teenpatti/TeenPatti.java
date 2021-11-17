package com.mpgames.zone.teenpatti;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.OnDisconnect;
import com.google.firebase.database.ValueEventListener;
import com.mpgames.zone.App;
import com.mpgames.zone.Auth;
import com.mpgames.zone.Connection;
import com.mpgames.zone.Database;
import com.mpgames.zone.Game;
import com.mpgames.zone.R;
import com.mpgames.zone.dialog.ConfirmDialog;
import com.mpgames.zone.room.Room;
import com.mpgames.zone.room.RoomManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class TeenPatti extends AppCompatActivity {
    private Context context = TeenPatti.this;
    private String TAG = "TeenPatti";
    private TextView totalAmountTextView;
    private boolean leader=false;
    private CardLayout card1Layout,card2Layout,card3Layout;
    private PlayerLayout player1View,player2View,player3View,player4View,player5View;
    private RoomManager roomManager;
    private Game game;
    private App app;
    private Room room;
    private Connection connection;
    private DatabaseReference roomRef,gameRef,playerRef;
    private ArrayList<String> players;
    private ArrayList<String> packed;
    private int[] layouts;
    private OnDisconnect online,playing;
    private boolean allOnline,allPlaying;
    public int maxBet = 8960;
    public int potLimit = 71680;
    private int currentAmount=70;
    private int totalAmount;
    private boolean myTurn = false;
    private boolean gameOver = false;
    private boolean blindPlay = true;
    private int blindMovesCount = 0;
    private int currentTurnPlayerIndex = 0;
    private DataSnapshot cardsSnapshot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teen_patti);
        initialiseViews();

        app = (App)getApplication();
        connection = Database.getConnection();

        String roomName = getIntent().getStringExtra("roomName");
        String[] playerString = getIntent().getStringArrayExtra("players");
        List<String> playerList = null;
        if(playerString!=null)
            playerList = Arrays.asList(playerString);
        players = new ArrayList<>();
        if(playerList!=null)
            players.addAll(playerList);
        packed = new ArrayList<>();
        layouts = new int[players.size()];
        for(int i=0 , l=2 ; i<players.size() && l<=5 ; i++){
            if(Auth.getUserName().equals(players.get(i))){
                layouts[i] = 1;
            }else{
                layouts[i] = l;
                l++;
            }
        }
        if(players.indexOf(Auth.getUserName())==0)
            myTurn = true;
        onTurnChange(myTurn);
        totalAmount = players.size()*70;
        totalAmountTextView.setText("Rs."+totalAmount);
        game = new Game(Game.TEEN_PATTI,roomName);
        if(roomName==null){
            finish();
            return;
        }
        if(Auth.getUserName().equals(roomName))
            leader=true;
        roomManager = new RoomManager(roomName);
        roomRef = Database.getRoomRef(roomName);
        gameRef = Database.getGameRef(Game.TEEN_PATTI,roomName);
        playerRef = roomRef.child("players").child(Auth.getUserName());
        findViewById(R.id.cardsLayout).setOnClickListener(view->{
            blindPlay = false;
            ((Button)findViewById(R.id.chaalbtn)).setText("Chaal");
            card1Layout.show();
            card2Layout.show();
            card3Layout.show();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        app.setContext(context);
        setUpEventListeners();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(app.context==context)
            app.setContext(null);
        removeListeners();
        playerRef.child("playing").setValue(false);
        /**if(app.call!=null){
            app.call.removeCallListener(callListener);
        }**/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        card1Layout.free();
        card2Layout.free();
        card3Layout.free();
        player1View.free();
        player2View.free();
        player3View.free();
        player4View.free();
        player5View.free();
        card1Layout = null;
        card2Layout = null;
        card3Layout = null;
        player1View = null;
        player2View = null;
        player3View = null;
        player4View = null;
        player5View = null;
        app = null;
        game = null;
        context = null;
        connection = null;
        roomRef = null;
        gameRef = null;
        playerRef = null;
        players = null;
        packed = null;
    }

    @Override
    public void onBackPressed() {
        ConfirmDialog confirmDialog = new ConfirmDialog(context,
                "Exit Game",
                "Do you want to exit the game?",
                () -> {
                    online.cancel();
                    playing.cancel();
                    roomManager.gameStarted(false);
                    roomManager.leftGame(Auth.getPlayer());
                    finish();
                },null);
        confirmDialog.show();
    }
    public void showClick(View v){
        if(!myTurn || gameOver)
            return;
        myTurn=false;
        Move move = new Move();
        move.setPlayerUserName(Auth.getUserName());
        move.setAction(Move.CHAAL);
        move.setAmount(currentAmount);
        game.writeMove(move);
    }
    public void packClick(View v){
        if(!myTurn || gameOver )
            return;
        myTurn=false;
        Move move = new Move();
        move.setPlayerUserName(Auth.getUserName());
        move.setAction(Move.PACK);
        move.setAmount(currentAmount);
        game.writeMove(move);
        findViewById(R.id.actionbar).setEnabled(false);
    }
    public void raiseClick(View v){
        if(!myTurn || gameOver)
            return;
        myTurn=false;
        Move move = new Move();
        move.setPlayerUserName(Auth.getUserName());
        move.setAction(Move.RAISE);
        move.setAmount(currentAmount*2);
        if(blindPlay){
            move.setAction(Move.BLIND);
            move.setAmount(currentAmount);
        }
        game.writeMove(move);

    }
    public void chaalClick(View v){
        if(!myTurn || gameOver)
            return;
        myTurn=false;
        Move move = new Move();
        move.setPlayerUserName(Auth.getUserName());
        move.setAction(Move.CHAAL);
        move.setAmount(currentAmount);
        if(blindPlay){
            move.setAction(Move.BLIND);
            move.setAmount(currentAmount/2);
        }
        game.writeMove(move);
    }

    private void setUpEventListeners(){
        roomRef.addValueEventListener(roomListener);
        gameRef.child("cards").addValueEventListener(cardsListener);
        connection.setEventListener(connectionListener);
        game.setMoveListener(moveListener);
    }
    private void removeListeners(){
        connection.removeEventListener();
        roomRef.removeEventListener(roomListener);
        gameRef.child("cards").removeEventListener(cardsListener);
        game.removeMoveListener();
    }
    private Game.MoveListener moveListener = moveObj -> {
        Move move = (Move) moveObj;
        currentAmount = move.getAmount();
        totalAmount+=move.getAmount();
        if(currentAmount==maxBet){
            findViewById(R.id.raisebtn).setEnabled(false);
        }
        if(totalAmount>potLimit){
            gameOver = true;
            declareWinner();
            totalAmountTextView.setText("Rs."+totalAmount);
        }else if(players.size()-packed.size()==2 && move.getAction()==Move.SHOW){
            gameOver = true;
            declareWinner();
        }else if(players.size()-packed.size()==1){
            gameOver = true;
            declareWinner();
        }else{
            if(move.getAction()==Move.BLIND){
                if(Auth.getUserName().equals(move.getPlayerUserName())){
                    blindMovesCount++;
                }
                currentAmount = move.getAmount()*2;
            }
            if(move.getAction()==Move.PACK){
                packed.add(move.getPlayerUserName());
                getPlayerLayout(layouts[players.indexOf(move.getPlayerUserName())]).setEnabled(false);
            }
            int nextPlayerIndex = players.indexOf(move.getPlayerUserName())+1;
            if(!(nextPlayerIndex<players.size()))
                nextPlayerIndex -= players.size();
            while (packed.contains(players.indexOf(nextPlayerIndex))){
                nextPlayerIndex++;
            }
            currentTurnPlayerIndex = nextPlayerIndex;
            if(players.indexOf(Auth.getUserName())==nextPlayerIndex)
                myTurn = true;

            totalAmountTextView.setText("Rs."+totalAmount);
            getPlayerLayout(layouts[players.indexOf(move.getPlayerUserName())]).setLastMove(move);
            onTurnChange(myTurn);
        }
    };
    Connection.EventListener connectionListener = new Connection.EventListener(){
        @Override
        public void onConnected() {
            playerRef.child("online").setValue(true);
            playerRef.child("playing").setValue(true);
            online= playerRef.child("online").onDisconnect();
            playing =  playerRef.child("playing").onDisconnect();
            online.setValue(false);
            playing.setValue(false);
        }
        @Override
        public void onDisconnected() {
        }
    };
    private ValueEventListener cardsListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if(!dataSnapshot.exists() && leader)
                distributeCards();
            else{
                cardsSnapshot = dataSnapshot;
                DataSnapshot cards = dataSnapshot.child(Auth.getUserName());
                Card card1 = cards.child("0").getValue(Card.class);
                Card card2 = cards.child("1").getValue(Card.class);
                Card card3 = cards.child("2").getValue(Card.class);
                card1Layout.setCard(card1);
                card2Layout.setCard(card2);
                card3Layout.setCard(card3);
            }
        }
        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.e(TAG,"Listener was cancelled "+databaseError.getMessage());
        }
    };
    private ValueEventListener roomListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            room = dataSnapshot.getValue(Room.class);
            int[] playerNo = {2};
            allOnline=true;
            allPlaying=true;
            final Object[] opponentLeft = {false,""};
            room.loopThroughPlayers((userName, player) -> {
                if(!player.isOnline())
                    allOnline=false;
                if(!player.isPlaying())
                    allPlaying=false;
                if(Auth.isCurrentPlayer(player)){
                    getPlayerLayout(1).setPlayer(player);
                }else{
                    getPlayerLayout(playerNo[0]).setPlayer(player);
                    playerNo[0]++;
                    if(player.isLeftGame()){
                        opponentLeft[0] =true;
                        opponentLeft[1] = player.getUserName();
                    }
                }
            });
            if((boolean)opponentLeft[0]){
                String name = (String) opponentLeft[1];
                Toast.makeText(context,name+" Left the Game",Toast.LENGTH_SHORT).show();
                online.cancel();
                playing.cancel();
                finish();
            }
        }
        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.e(TAG,"Listener was cancelled "+databaseError.getMessage());
        }
    };

    private void onTurnChange(boolean myTurn){
        if(myTurn){
            ((TextView)findViewById(R.id.turntxt)).setText("Your Turn");
        }else{
            String opponentName = "Opponent";
            if(room!=null)
                opponentName = room.getPlayers().get(players.get(currentTurnPlayerIndex)).getName();
            ((TextView)findViewById(R.id.turntxt)).setText(opponentName+"'s Turn");
        }
        if(myTurn && blindMovesCount==4){
            blindPlay=false;
            ((Button)findViewById(R.id.chaalbtn)).setText("Chaal");
            card1Layout.show();
            card2Layout.show();
            card3Layout.show();
        }
    }

    private boolean isSequenceFollowed(int a,int b,int c){
        ArrayList<Integer> arrayList = new ArrayList<>();
        arrayList.add(a);
        arrayList.add(b);
        arrayList.add(c);
        Collections.sort(arrayList);
        if(arrayList.get(0)+1==arrayList.get(1) && arrayList.get(0)+2==arrayList.get(2))
            return true;
        return false;
    }

    private int getHighestScore(ArrayList<Integer> scoreList){
        ArrayList<Integer> scores = new ArrayList<>();
        scores.addAll(scoreList);
        Collections.sort(scores);
        return scores.get(scores.size()-1);
    }

    private int getScores(Card[] cards){
        int n1=cards[0].getNumber(),n2=cards[1].getNumber(),n3=cards[2].getNumber();
        int t1=cards[0].getType(),t2=cards[1].getType(),t3=cards[2].getType();
        if(n1==n2 && n2==n3){
            int a = n1;
            String text = a<10?"0"+a:""+a;
            return Integer.parseInt(""+5+text+00+00);
        }else if(t1==t2 && t2==t3 && isSequenceFollowed(n1,n2,n3)){
            int x = n1>n2?n1:n2;
            int a = x>n3?x:n3;
            int b = x>n3?n3:x;
            String text1 = a<10 ? "0"+a : ""+a;
            String text2 = b<10 ? "0"+b : ""+b;
            return Integer.parseInt(""+4+text1+text2+00);
        }else if(isSequenceFollowed(n1,n2,n3)){
            int x = n1>n2?n1:n2;
            int a = x>n3?x:n3;
            int b = x>n3?n3:x;
            String text1 = a<10 ? "0"+a : ""+a;
            String text2 = b<10 ? "0"+b : ""+b;
            return Integer.parseInt(""+3+text1+text2+00);
        }else if(t1==t2 & t2==t3){
            int x = n1>n2?n1:n2;
            int a = x>n3?x:n3;
            int b = x>n3?n3:x;
            int c = n1>n2?n2:n1;
            String text1 = a<10 ? "0"+a : ""+a;
            String text2 = b<10 ? "0"+b : ""+b;
            String text3 = c<10 ? "0"+c : ""+c;
            return Integer.parseInt(""+2+text1+text2+text3);
        }else if(n1==n2 || n2==n3 || n3==n1){
            int a = n1==n2?n1:0;
            int b = n1==n2?n3:0;
            a = n2==n3&&a==0?n2:0;
            b = n2==n3&&b==0?n1:0;
            a = n3==n1&&a==0?n3:0;
            b = n3==n1&&b==0?n2:0;
            String text1= a<10?"0"+a:""+a;
            String text2= b<10?"0"+b:""+b;
            return Integer.parseInt(""+1+text1+text2+00);
        }
        int x = n1>n2?n1:n2;
        int a = x>n3?x:n3;
        int b = x>n3?n3:x;
        int c = n1>n2?n2:n1;
        String text1 = a<10 ? "0"+a : ""+a;
        String text2 = b<10 ? "0"+b : ""+b;
        String text3 = c<10 ? "0"+c : ""+c;
        return Integer.parseInt(""+0+text1+text2+text3);
    }

    private String getWinType(int x){
        int type = Integer.parseInt((""+x).substring(0,1));
        if(type==5)
            return "Trail (three of a kind)";
        else if(type==4)
            return "Pure Sequence (Straight Flush)";
        else if(type==3)
            return "Sequence (Straight)";
        else if(type==2)
            return "Color (Flush)";
        else if(type==1)
            return "Pair (two of a kind)";
        return "High Card";
    }

    private void declareWinner(){
        ArrayList<String> eligiblePlayers = new ArrayList<>();
        ArrayList<Integer> scores = new ArrayList<>();
        for(int i=0 ; i<players.size() ; i++){
            String player = players.get(i);
            if(!packed.contains(player)){
                Card c1 = cardsSnapshot.child(player).child("0").getValue(Card.class);
                Card c2 = cardsSnapshot.child(player).child("1").getValue(Card.class);
                Card c3 = cardsSnapshot.child(player).child("2").getValue(Card.class);
                int score = getScores(new Card[]{c1,c2,c3});
                eligiblePlayers.add(players.get(i));
                scores.add(score);
            }
        }
        int winnerScore = getHighestScore(scores);
        String winner = eligiblePlayers.get(scores.indexOf(winnerScore));
        if(Auth.getUserName().equals(winner))
            Snackbar.make(totalAmountTextView,"You WON by "+getWinType(winnerScore),Snackbar.LENGTH_INDEFINITE).show();
        else
            Snackbar.make(totalAmountTextView,winner+" WON by "+getWinType(winnerScore),Snackbar.LENGTH_INDEFINITE).show();
    }

    private void distributeCards(){
        ArrayList<String> allCards = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.cards)));
        Map<String,Map<String,Card>> playerCards = new HashMap<>();
        for(int p=0;p<players.size();p++){
            Map<String,Card> cards = new HashMap<>();
            for(int i=0;i<3;i++){
                Random random = new Random();
                int index = random.nextInt(allCards.size());
                cards.put(""+i,new Card(allCards.get(index)));
                allCards.remove(index);
            }
            playerCards.put(players.get(p),cards);
        }
        gameRef.child("cards").setValue(playerCards);
    }

    private PlayerLayout getPlayerLayout(int layoutNo){
        if(layoutNo==1)
            return player1View;
        else if(layoutNo==2)
            return player2View;
        else if(layoutNo==3)
            return player3View;
        else if(layoutNo==4)
            return player4View;
        else if(layoutNo==5)
            return player5View;
        return null;
    }

    private void initialiseViews(){
        totalAmountTextView = findViewById(R.id.totalamounttxt);
        card1Layout = findViewById(R.id.card1Layout);
        card2Layout = findViewById(R.id.card2Layout);
        card3Layout = findViewById(R.id.card3Layout);
        player1View = findViewById(R.id.player1Layout);
        player2View = findViewById(R.id.player2Layout);
        player3View = findViewById(R.id.player3Layout);
        player4View = findViewById(R.id.player4Layout);
        player5View = findViewById(R.id.player5Layout);
    }
}
