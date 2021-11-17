package com.mpgames.zone.chess;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.OnDisconnect;
import com.mpgames.zone.App;
import com.mpgames.zone.Auth;
import com.mpgames.zone.Connection;
import com.mpgames.zone.Database;
import com.mpgames.zone.Game;
import com.mpgames.zone.PermissionsActivity;
import com.mpgames.zone.PlayerTile;
import com.mpgames.zone.room.Player;
import com.mpgames.zone.room.Room;
import com.mpgames.zone.room.RoomManager;
import com.mpgames.zone.dialog.ConfirmDialog;
import com.mpgames.zone.R;
import com.mpgames.zone.dialog.PromotionDialog;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallListener;

import java.util.ArrayList;
import java.util.List;

public class ChessActivity extends AppCompatActivity {
    private static String TAG = "ChessActivity";
    private Context context = ChessActivity.this;

    private App app;

    private ImageView
            box11,box12,box13,box14,box15,box16,box17,box18,
            box21,box22,box23,box24,box25,box26,box27,box28,
            box31,box32,box33,box34,box35,box36,box37,box38,
            box41,box42,box43,box44,box45,box46,box47,box48,
            box51,box52,box53,box54,box55,box56,box57,box58,
            box61,box62,box63,box64,box65,box66,box67,box68,
            box71,box72,box73,box74,box75,box76,box77,box78,
            box81,box82,box83,box84,box85,box86,box87,box88;
    private ImageView lost1img,lost2img;
    private PlayerTile player1Tile,player2Tile;
    private TextView turntxt;
    private ImageView voiceChatIndicator;

    private View view;

    private DatabaseReference roomRef, playerRef;
    private GameEngine gameEngine;

    private int selectedCellRow = 0, selectedCellColumn = 0;
    private boolean cellSelected = false;
    private ArrayList<Integer> selectedPieceMoves = null;

    private boolean myTurn = false;
    private boolean leader = false;

    private RoomManager roomManager;
    private Game game;
    private Connection connection;

    private LinearLayout mainContainer;
    private boolean allOnline,allPlaying;

    private OnDisconnect online,playing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chess);
        initialiseViews();
        setUpClickListeners();

        app = (App) getApplication();
        connection = Database.getConnection();

        String roomName = getIntent().getStringExtra("roomName");
        roomManager = new RoomManager(roomName);
        game = new Game(Game.CHESS,roomName);

        if(Auth.getUserName().equals(roomName)){
            leader = true;
            turntxt.setText("Opponent's Turn");
        }else{
            myTurn = true;
        }

        gameEngine = new GameEngine(roomName);
        if(leader)
            gameEngine.leader = true;
        initialiseGameEngine();
        roomRef = Database.getRoomRef(roomName);
        playerRef = roomRef.child("players").child(Auth.getUserName());
    }

    @Override
    protected void onStart() {
        super.onStart();
        app.setContext(context);
        setUpEventListeners();
        if(app.call!=null){
            voiceChatIndicator.setImageResource(R.drawable.ic_mic);
            app.call.addCallListener(callListener);
        }else
            voiceChatIndicator.setImageResource(R.drawable.ic_mic_off);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(app.context==context){
            app.setContext(null);
        }
        roomManager.removeEventListener();
        gameEngine.setEventListener(null);
        game.removeMoveListener();
        connection.removeEventListener();
        playerRef.child("playing").setValue(false);
        if(app.call!=null){
            app.call.removeCallListener(callListener);
        }
    }


    //On User Input
    @Override
    public void onBackPressed() {
        ConfirmDialog confirmDialog = new ConfirmDialog(context,
                "Exit Game",
                "Do you want to exit the game?",
                new Runnable() {
                    @Override
                    public void run() {
                        online.cancel();
                        playing.cancel();
                        roomManager.gameStarted(false);
                        roomManager.leftGame(Auth.getPlayer());
                        finish();
                    }
                },null);
        confirmDialog.show();
    }
    private void userClick(int row, int column){
        if(!myTurn){
            return;
        }
        if(cellSelected){
            if(selectedCellRow ==row && selectedCellColumn ==column){
                dehighlightMoves();
                return;
            }else{
                if(!gameEngine.cellIsFilled(selectedCellRow, selectedCellColumn)){
                    dehighlightMoves();
                    return;
                }
                ArrayList<Integer> moves = selectedPieceMoves;
                if(moves.contains(gameEngine.getKey(row,column))){
                    final Piece piece = gameEngine.getCellPiece(selectedCellRow, selectedCellColumn);
                    final Move move = new Move();
                    move.setFromKey(piece.getRow(),piece.getColumn());
                    move.setToKey(row, column);
                    move.setPlayerUserName(Auth.getUserName());
                    if(piece.isPawn() && row==1){
                        int color = 0;
                        if(leader){
                            color=1;
                        }else{
                            color=2;
                        }
                        PromotionDialog dialog = new PromotionDialog(context,color);
                        dialog.setResponseListener(new PromotionDialog.ResponseListener() {
                            @Override
                            public void onPieceSelected(int type) {
                                move.setPromoteTo(type);
                                if(leader){
                                    move.setPlayerNo(1);
                                }else{
                                    move.setPlayerNo(2);
                                    move.switchSides();
                                }
                                gameEngine.writeMove(move);
                            }
                        });
                        dialog.show();
                    }else{
                        if(leader){
                            move.setPlayerNo(1);
                        }else{
                            move.setPlayerNo(2);
                            move.switchSides();
                        }
                        gameEngine.writeMove(move);
                    }
                }
                dehighlightMoves();
            }
        }else{
            if(gameEngine.cellIsFilled(row,column)&& gameEngine.getCellPiece(row,column).getPlayer()==1 ){
                gameEngine.hideLastMove();
                selectedCellRow = row;
                selectedCellColumn = column;
                cellSelected =true;
                ArrayList<Integer> moves = gameEngine.getValidMoves(selectedCellRow, selectedCellColumn);
                selectedPieceMoves = moves;
                highlightMoves();
            }
        }
    }
    public void voiceChatClick(View v){
        if(app.call!=null){
            app.call.hangup();
            return;
        }
        if(app.checkPermissions()){
            app.call = app.callClient.callConference(roomManager.getRoomName());
            app.call.addCallListener(callListener);
        }else{
            Intent intent = new Intent(context, PermissionsActivity.class);
            startActivityForResult(intent,7);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==7 && resultCode==RESULT_OK){
            app.call = app.callClient.callConference(roomManager.getRoomName());
            app.call.addCallListener(callListener);
        }
    }


    //Setting Up Event Listeners
    private void setUpEventListeners(){
        roomManager.setEventListener(roomEventListener);
        game.setMoveListener(moveListener);
        connection.setEventListener(connectionListener);
        gameEngine.setEventListener(gameEventListener);
    }
    GameEngine.EventListener gameEventListener = new GameEngine.EventListener() {
        @Override
        public void onCheck(int playerNo) {
            if(playerNo==1){
                Snackbar.make(view,"CHECK ON YOU",Snackbar.LENGTH_LONG).show();
            }else{
                Snackbar.make(view,"CHECK ON OPPONENT",Snackbar.LENGTH_LONG).show();
            }
        }
        @Override
        public void onCheckMate(int playerNo) {
            if(playerNo==1){
                Snackbar.make(view,"CHECKMATE ! YOU LOST THE GAME",Snackbar.LENGTH_INDEFINITE).show();
            }else{
                Snackbar.make(view,"CHECKMATE ! YOU WON THE GAME",Snackbar.LENGTH_INDEFINITE).show();
            }
        }
        @Override
        public void onDraw() {
            Snackbar.make(view,"ITS A DRAW",Snackbar.LENGTH_INDEFINITE).show();
        }

        @Override
        public void onPieceDies(Piece piece) {
            if(leader){
                if(piece.getPlayer()==1){
                    lost1img.setImageResource(getImageResource(piece.getType(),1));
                }else if(piece.getPlayer()==2){
                    lost2img.setImageResource(getImageResource(piece.getType(),2));
                }
            }else{
                if(piece.getPlayer()==1){
                    lost1img.setImageResource(getImageResource(piece.getType(),2));
                }else if(piece.getPlayer()==2){
                    lost2img.setImageResource(getImageResource(piece.getType(),1));
                }
            }

        }
    };
    RoomManager.EventListener roomEventListener = new RoomManager.EventListener() {
        @Override
        public void onDataChange(Room room) {
            allOnline=true;
            allPlaying=true;
            final Object[] opponentLeft = {false,""};
            room.loopThroughPlayers(new Room.PlayerLooper() {
                @Override
                public void onPlayerTurn(String userName, Player player) {
                    if(!player.isOnline())
                        allOnline=false;
                    if(!player.isPlaying())
                        allPlaying=false;
                    if(Auth.isCurrentPlayer(player)){
                        updatePlayerInfo(1, player);
                    }else{
                        updatePlayerInfo(2,player);
                        if(player.isLeftGame()){
                            opponentLeft[0] =true;
                            opponentLeft[1] = player.getUserName();
                        }
                    }
                }
            });
            if((boolean)opponentLeft[0]){
                Toast.makeText(context,"Opponent Left the Game",Toast.LENGTH_SHORT).show();
                online.cancel();
                playing.cancel();
                finish();
            }
        }
        @Override
        public void onRoomDeleted() {
            Toast.makeText(context,"Leader left the Room",Toast.LENGTH_SHORT).show();
            online.cancel();
            playing.cancel();
            finish();
        }
    };
    Connection.EventListener connectionListener = new Connection.EventListener(){
        @Override
        public void onConnected() {
            playerRef.child("playing").setValue(true);
            playerRef.child("online").setValue(true);
            online= playerRef.child("online").onDisconnect();
            playing =  playerRef.child("playing").onDisconnect();
            online.setValue(false);
            playing.setValue(false);
        }
        @Override
        public void onDisconnected() {
        }
    };
    CallListener callListener = new CallListener() {
        @Override
        public void onCallProgressing(Call call) {
            voiceChatIndicator.setImageResource(R.drawable.ic_mic_none);
        }
        @Override
        public void onCallEstablished(Call call) {
            roomManager.joinedVoiceChat(Auth.getPlayer());
            Toast.makeText(context,"Voice Chat Connected",Toast.LENGTH_SHORT).show();
            voiceChatIndicator.setImageResource(R.drawable.ic_mic);
        }
        @Override
        public void onCallEnded(Call call) {
            app.call.removeCallListener(callListener);
            app.call = null;
            roomManager.leftVoiceChat(Auth.getPlayer());
            Toast.makeText(context,"Voice Chat Ended",Toast.LENGTH_SHORT).show();
            voiceChatIndicator.setImageResource(R.drawable.ic_mic_off);
        }
        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> list) {
        }
    };

    Game.MoveListener moveListener = new Game.MoveListener(){
        @Override
        public void onMoveAdded(Object moveObj) {
            Move move = (Move) moveObj;
            ImageView imageView;
            if(Auth.getUserName().equals(move.getPlayerUserName())){
                if(move.getPlayerNo()==2){
                    move.setPlayerNo(1);
                    move.switchSides();
                }
            }else{
                if(move.getPlayerNo()==1){
                    move.setPlayerNo(2);
                    move.switchSides();
                }
            }
            imageView = getBoxImageView(move.toKey);
            gameEngine.executeMove(move,imageView);
            switchTurn();
        }
    };
    private void switchTurn(){
        if(myTurn){
            turntxt.setText("Opponent's Turn");
            myTurn = false;
        } else {
            turntxt.setText("Your Turn");
            myTurn = true;
        }
    }


    private int getImageResource(int type, int color){
        if(color==1){
            if(type==0){
                return R.drawable.pawn;
            }else if(type==1){
                return R.drawable.rook;
            }else if(type==2){
                return R.drawable.knight;
            }else if(type==3){
                return R.drawable.bishop;
            }else if(type==4){
                return R.drawable.queen;
            }
        }else if(color==2){
            if(type==0){
                return R.drawable.pawn2;
            }else if(type==1){
                return R.drawable.rook2;
            }else if(type==2){
                return R.drawable.knight2;
            }else if(type==3){
                return R.drawable.bishop2;
            }else if(type==4){
                return R.drawable.queen2;
            }
        }
        return 0;
    }
    private void highlightMoves(){
        int row = selectedCellRow;
        int column = selectedCellColumn;
        ArrayList<Integer> keyList = selectedPieceMoves;
        getBoxImageView(row, column).setBackgroundColor(getResources().getColor(R.color.chess_blue_box));
        for(int i=0 ; i<keyList.size() ; i++){
            int key = keyList.get(i);
            int color = getResources().getColor(gameEngine.getColorID(key,true));
            getBoxImageView(key).setBackgroundColor(color);
        }
    }
    private void dehighlightMoves(){
        ArrayList<Integer> moves = selectedPieceMoves;
        moves.add(gameEngine.getKey(selectedCellRow, selectedCellColumn));
        selectedCellRow =0;
        selectedCellColumn =0;
        cellSelected =false;
        selectedPieceMoves = null;
        for(int i=0 ; i<moves.size() ; i++){
            int key = moves.get(i);
            int color = getResources().getColor(gameEngine.getColorID(key,false));
            getBoxImageView(key).setBackgroundColor(color);
        }
    }


    //Update Players Information
    private void updatePlayerInfo(int tileNo, Player player){
        PlayerTile tile = getPlayerTile(tileNo);
        if(tile!=null){
            tile.setName(player.getName());
            tile.setDescription(player.getUserName());
            tile.setStatusIndicator(player.isOnline(),player.isPlaying());
            tile.setProfilePhoto(player.getPhotoUrl());
            if(allOnline && allPlaying)
                mainContainer.setBackgroundColor(getResources().getColor(R.color.green));
            else if(allOnline)
                mainContainer.setBackgroundColor(getResources().getColor(R.color.yellow));
            else
                mainContainer.setBackgroundColor(getResources().getColor(R.color.red));
        }
    }
    private PlayerTile getPlayerTile(int tileNo){
        PlayerTile tile = null;
        if(tileNo==1)
            tile = player1Tile;
        else if(tileNo==2)
            tile = player2Tile;
        return tile;
    }

    //Initialisation
    void initialiseViews(){
        mainContainer = findViewById(R.id.chessBackground);
        view = mainContainer;
        turntxt = findViewById(R.id.turntxt);
        voiceChatIndicator = findViewById(R.id.voicechatimg);
        player1Tile = new PlayerTile(
                findViewById(R.id.p1nametxt),
                findViewById(R.id.p1desctxt),
                findViewById(R.id.p1statusimg),
                findViewById(R.id.p1playerimg));
        player2Tile = new PlayerTile(
                findViewById(R.id.p2nametxt),
                findViewById(R.id.p2desctxt),
                findViewById(R.id.p2statusimg),
                findViewById(R.id.p2playerimg));
        lost1img = findViewById(R.id.lost1img);
        lost2img = findViewById(R.id.lost2img);
        box11 = findViewById(R.id.box11);
        box12 = findViewById(R.id.box12);
        box13 = findViewById(R.id.box13);
        box14 = findViewById(R.id.box14);
        box15 = findViewById(R.id.box15);
        box16 = findViewById(R.id.box16);
        box17 = findViewById(R.id.box17);
        box18 = findViewById(R.id.box18);
        box21 = findViewById(R.id.box21);
        box22 = findViewById(R.id.box22);
        box23 = findViewById(R.id.box23);
        box24 = findViewById(R.id.box24);
        box25 = findViewById(R.id.box25);
        box26 = findViewById(R.id.box26);
        box27 = findViewById(R.id.box27);
        box28 = findViewById(R.id.box28);
        box31 = findViewById(R.id.box31);
        box32 = findViewById(R.id.box32);
        box33 = findViewById(R.id.box33);
        box34 = findViewById(R.id.box34);
        box35 = findViewById(R.id.box35);
        box36 = findViewById(R.id.box36);
        box37 = findViewById(R.id.box37);
        box38 = findViewById(R.id.box38);
        box41 = findViewById(R.id.box41);
        box42 = findViewById(R.id.box42);
        box43 = findViewById(R.id.box43);
        box44 = findViewById(R.id.box44);
        box45 = findViewById(R.id.box45);
        box46 = findViewById(R.id.box46);
        box47 = findViewById(R.id.box47);
        box48 = findViewById(R.id.box48);
        box51 = findViewById(R.id.box51);
        box52 = findViewById(R.id.box52);
        box53 = findViewById(R.id.box53);
        box54 = findViewById(R.id.box54);
        box55 = findViewById(R.id.box55);
        box56 = findViewById(R.id.box56);
        box57 = findViewById(R.id.box57);
        box58 = findViewById(R.id.box58);
        box61 = findViewById(R.id.box61);
        box62 = findViewById(R.id.box62);
        box63 = findViewById(R.id.box63);
        box64 = findViewById(R.id.box64);
        box65 = findViewById(R.id.box65);
        box66 = findViewById(R.id.box66);
        box67 = findViewById(R.id.box67);
        box68 = findViewById(R.id.box68);
        box71 = findViewById(R.id.box71);
        box72 = findViewById(R.id.box72);
        box73 = findViewById(R.id.box73);
        box74 = findViewById(R.id.box74);
        box75 = findViewById(R.id.box75);
        box76 = findViewById(R.id.box76);
        box77 = findViewById(R.id.box77);
        box78 = findViewById(R.id.box78);
        box81 = findViewById(R.id.box81);
        box82 = findViewById(R.id.box82);
        box83 = findViewById(R.id.box83);
        box84 = findViewById(R.id.box84);
        box85 = findViewById(R.id.box85);
        box86 = findViewById(R.id.box86);
        box87 = findViewById(R.id.box87);
        box88 = findViewById(R.id.box88);
    }
    void setUpClickListeners(){
        box11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(1,1);
            }
        });
        box12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(1,2);
            }
        });
        box13.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(1,3);
            }
        });
        box14.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(1,4);
            }
        });
        box15.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(1,5);
            }
        });
        box16.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(1,6);
            }
        });
        box17.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(1,7);
            }
        });
        box18.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(1,8);
            }
        });
        box21.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(2,1);
            }
        });
        box22.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(2,2);
            }
        });
        box23.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(2,3);
            }
        });
        box24.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(2,4);
            }
        });
        box25.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(2,5);
            }
        });
        box26.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(2,6);
            }
        });
        box27.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(2,7);
            }
        });
        box28.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(2,8);
            }
        });
        box31.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(3,1);
            }
        });
        box32.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(3,2);
            }
        });
        box33.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(3,3);
            }
        });
        box34.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(3,4);
            }
        });
        box35.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(3,5);
            }
        });
        box36.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(3,6);
            }
        });
        box37.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(3,7);
            }
        });
        box38.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(3,8);
            }
        });
        box41.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(4,1);
            }
        });
        box42.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(4,2);
            }
        });
        box43.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(4,3);
            }
        });
        box44.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(4,4);
            }
        });
        box45.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(4,5);
            }
        });
        box46.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(4,6);
            }
        });
        box47.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(4,7);
            }
        });
        box48.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(4,8);
            }
        });
        box51.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(5,1);
            }
        });
        box52.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(5,2);
            }
        });
        box53.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(5,3);
            }
        });
        box54.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(5,4);
            }
        });
        box55.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(5,5);
            }
        });
        box56.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(5,6);
            }
        });
        box57.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(5,7);
            }
        });
        box58.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(5,8);
            }
        });
        box61.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(6,1);
            }
        });
        box62.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(6,2);
            }
        });
        box63.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(6,3);
            }
        });
        box64.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(6,4);
            }
        });
        box65.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(6,5);
            }
        });
        box66.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(6,6);
            }
        });
        box67.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(6,7);
            }
        });
        box68.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(6,8);
            }
        });
        box71.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(7,1);
            }
        });
        box72.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(7,2);
            }
        });
        box73.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(7,3);
            }
        });
        box74.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(7,4);
            }
        });
        box75.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(7,5);
            }
        });
        box76.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(7,6);
            }
        });
        box77.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(7,7);
            }
        });
        box78.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(7,8);
            }
        });
        box81.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(8,1);
            }
        });
        box82.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(8,2);
            }
        });
        box83.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(8,3);
            }
        });
        box84.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(8,4);
            }
        });
        box85.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(8,5);
            }
        });
        box86.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(8,6);
            }
        });
        box87.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(8,7);
            }
        });
        box88.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(8,8);
            }
        });
    }
    void initialiseGameEngine(){
        int[] rows = {1,2,7,8};
        for(int i=0 ; i<rows.length ; i++){
            int row = rows[i];
            for(int column=1 ; column<9 ; column++){
                gameEngine.createPiece(row,column,getBoxImageView(row,column));
            }
        }
    }


    //Get ImageView of Specific Box
    private ImageView getBoxImageView(int row, int column){
        if(row==1 && column==1) return box11;
        if(row==1 && column==2) return box12;
        if(row==1 && column==3) return box13;
        if(row==1 && column==4) return box14;
        if(row==1 && column==5) return box15;
        if(row==1 && column==6) return box16;
        if(row==1 && column==7) return box17;
        if(row==1 && column==8) return box18;
        if(row==2 && column==1) return box21;
        if(row==2 && column==2) return box22;
        if(row==2 && column==3) return box23;
        if(row==2 && column==4) return box24;
        if(row==2 && column==5) return box25;
        if(row==2 && column==6) return box26;
        if(row==2 && column==7) return box27;
        if(row==2 && column==8) return box28;
        if(row==3 && column==1) return box31;
        if(row==3 && column==2) return box32;
        if(row==3 && column==3) return box33;
        if(row==3 && column==4) return box34;
        if(row==3 && column==5) return box35;
        if(row==3 && column==6) return box36;
        if(row==3 && column==7) return box37;
        if(row==3 && column==8) return box38;
        if(row==4 && column==1) return box41;
        if(row==4 && column==2) return box42;
        if(row==4 && column==3) return box43;
        if(row==4 && column==4) return box44;
        if(row==4 && column==5) return box45;
        if(row==4 && column==6) return box46;
        if(row==4 && column==7) return box47;
        if(row==4 && column==8) return box48;
        if(row==5 && column==1) return box51;
        if(row==5 && column==2) return box52;
        if(row==5 && column==3) return box53;
        if(row==5 && column==4) return box54;
        if(row==5 && column==5) return box55;
        if(row==5 && column==6) return box56;
        if(row==5 && column==7) return box57;
        if(row==5 && column==8) return box58;
        if(row==6 && column==1) return box61;
        if(row==6 && column==2) return box62;
        if(row==6 && column==3) return box63;
        if(row==6 && column==4) return box64;
        if(row==6 && column==5) return box65;
        if(row==6 && column==6) return box66;
        if(row==6 && column==7) return box67;
        if(row==6 && column==8) return box68;
        if(row==7 && column==1) return box71;
        if(row==7 && column==2) return box72;
        if(row==7 && column==3) return box73;
        if(row==7 && column==4) return box74;
        if(row==7 && column==5) return box75;
        if(row==7 && column==6) return box76;
        if(row==7 && column==7) return box77;
        if(row==7 && column==8) return box78;
        if(row==8 && column==1) return box81;
        if(row==8 && column==2) return box82;
        if(row==8 && column==3) return box83;
        if(row==8 && column==4) return box84;
        if(row==8 && column==5) return box85;
        if(row==8 && column==6) return box86;
        if(row==8 && column==7) return box87;
        if(row==8 && column==8) return box88;
        return null;
    }
    private ImageView getBoxImageView(int key){
        int r = gameEngine.getRowFromKey(key);
        int c = gameEngine.getColumnFromKey(key);
        return getBoxImageView(r,c);
    }
}

