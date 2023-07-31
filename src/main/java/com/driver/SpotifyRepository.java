package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class SpotifyRepository {
    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
    public HashMap<Playlist, List<Song>> playlistSongMap;
    public HashMap<Playlist, List<User>> playlistListenerMap;
    public HashMap<User, Playlist> creatorPlaylistMap;
    public HashMap<User, List<Playlist>> userPlaylistMap;
    public HashMap<Song, List<User>> songLikeMap;

    public List<User> users;
    public List<Song> songs;
    public List<Playlist> playlists;
    public List<Album> albums;
    public List<Artist> artists;

    public SpotifyRepository(){
        //To avoid hitting apis multiple times, initialize all the hashmaps here with some dummy data
        artistAlbumMap = new HashMap<>();
        albumSongMap = new HashMap<>();
        playlistSongMap = new HashMap<>();
        playlistListenerMap = new HashMap<>();
        creatorPlaylistMap = new HashMap<>();
        userPlaylistMap = new HashMap<>();
        songLikeMap = new HashMap<>();

        users = new ArrayList<>();
        songs = new ArrayList<>();
        playlists = new ArrayList<>();
        albums = new ArrayList<>();
        artists = new ArrayList<>();
    }

    public User createUser(String name, String mobile) {
        User user=new User(name,mobile);
        users.add(user);
        return user;
    }

    public Artist createArtist(String name) {
        Artist artist=new Artist(name);
        artists.add(artist);
        return artist;
    }

    public Album createAlbum(String title, String artistName) {
        Album album=new Album(title);
        Artist artist=new Artist(artistName);
        if(artists.contains(artist)){
            List<Album>albumList=artistAlbumMap.getOrDefault(artist,new ArrayList<>());
            albumList.add(album);
            artistAlbumMap.put(artist,albumList);
        }else{
            artists.add(artist);
            List<Album>albumList=new ArrayList<>();
            albumList.add(album);
            artistAlbumMap.put(artist,albumList);
        }
        return album;
    }

    public Song createSong(String title, String albumName, int length) throws Exception{
        Album album =new Album(albumName);
        Song song=new Song(title,length);
        if(albums.contains(album)){
            songs.add(song);
            List<Song>songList=albumSongMap.getOrDefault(album,new ArrayList<>());
            songList.add(song);
            albumSongMap.put(album,songList);
        }else{
            throw new Exception("Album does not exist");
        }
        return song;
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        //Create a playlist with given title and add all songs having the given length in the database to that playlist
        //The creater of the playlist will be the given user and will also be the only listener at the time of playlist creation
        //If the user does not exist, throw "User does not exist" exception
        Playlist playlist=new Playlist(title);
        playlists.add(playlist);
        List<Song>songList=new ArrayList<>();

        for(Song song:songs){
            if(song.getLength()==length){
                songList.add(song);
            }
        }
        playlistSongMap.put(playlist,songList);
        boolean userfound=false;
        for(User user:users){
            if(Objects.equals(user.getMobile(), mobile)){
                userfound=true;
                creatorPlaylistMap.put(user,playlist);
                List<Playlist>playlists1=userPlaylistMap.getOrDefault(user,new ArrayList<>());
                playlists1.add(playlist);
                userPlaylistMap.put(user,playlists1);
                break;
            }
        }
        if(!userfound){
            throw new Exception("User does not exist");
        }
        return playlist;
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        //Create a playlist with given title and add all songs having the given titles in the database to that playlist
        //The creater of the playlist will be the given user and will also be the only listener at the time of playlist creation
        //If the user does not exist, throw "User does not exist" exception
        Playlist playlist=new Playlist(title);
        playlists.add(playlist);
        List<Song>songList=new ArrayList<>();
        for(Song song:songs){
            if(songTitles.contains(song.getTitle())){
                songList.add(song);
            }
        }
        playlistSongMap.put(playlist,songList);
        boolean userfound=false;
        for(User user:users){
            if(Objects.equals(user.getMobile(), mobile)){
                userfound=true;
                creatorPlaylistMap.put(user,playlist);
                List<Playlist>playlists1=userPlaylistMap.getOrDefault(user,new ArrayList<>());
                playlists1.add(playlist);
                userPlaylistMap.put(user,playlists1);
                break;
            }
        }
        if(!userfound){
            throw new Exception("User does not exist");
        }
        return playlist;
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        //Find the playlist with given title and add user as listener of that playlist and update user accordingly
        //If the user is creater or already a listener, do nothing
        //If the user does not exist, throw "User does not exist" exception
        //If the playlist does not exists, throw "Playlist does not exist" exception
        // Return the playlist after updating
        User user=null;
        Playlist playlist=null;
        for(User u1:users){
            if(u1.getMobile().equals(mobile)){
                user=u1;
                break;
            }
        }

        for(Playlist p1:playlists){
            if(p1.getTitle().equals(playlistTitle)){
                playlist=p1;
                break;
            }
        }
        if(user==null){
            throw new Exception("User does not exist");
        }
        if(playlist==null){
            throw new Exception("Playlist does not exist");
        }
        if(!creatorPlaylistMap.containsKey(user)){
            creatorPlaylistMap.put(user,playlist);
            List<Playlist>playlists1=userPlaylistMap.getOrDefault(user,new ArrayList<>());
            playlists1.add(playlist);
            userPlaylistMap.put(user,playlists1);
        }
        return playlist;
    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
        //The user likes the given song. The corresponding artist of the song gets auto-liked
        //A song can be liked by a user only once. If a user tried to like a song multiple times, do nothing
        //However, an artist can indirectly have multiple likes from a user, if the user has liked multiple songs of that artist.
        //If the user does not exist, throw "User does not exist" exception
        //If the song does not exist, throw "Song does not exist" exception
        //Return the song after updating

        User user=null;
        Song song=null;
        for(User u1:users){
            if(u1.getMobile().equals(mobile)){
                user=u1;
                break;
            }
        }
        for(Song s1:songs){
            if(s1.getTitle().equals(songTitle)){
                song=s1;
                break;
            }
        }
        if(user==null){
            throw new Exception("User does not exist");
        }
        if(song==null){
            throw new Exception("Song does not exist");
        }
        List<User>userList=songLikeMap.getOrDefault(song,new ArrayList<>());
        if(!userList.contains(user)){
            userList.add(user);
            songLikeMap.put(song,userList);
            song.setLikes(song.getLikes()+1);

            Album album=new Album();
            for(Album a1:albumSongMap.keySet()){
                List<Song>s1=albumSongMap.get(a1);
                if(s1.contains(song)){
                    album=a1;
                    break;
                }
            }

            Artist artist=new Artist();
            for(Artist a1:artistAlbumMap.keySet()){
                List<Album>alb=artistAlbumMap.get(a1);
                if(alb.contains(album)){
                    artist=a1;
                    break;
                }

            }
            artist.setLikes(artist.getLikes()+1);
        }
        return song;

    }

    public String mostPopularArtist() {
        int max=0;
        String name="";
        for(Artist ar:artists){
            if(ar.getLikes()>max){
                max=ar.getLikes();
                name=ar.getName();
            }
        }
        return name;
    }

    public String mostPopularSong() {
        int max=0;
        String name="";
        for(Song s1:songs){
            if(s1.getLikes()>max){
                max= s1.getLikes();
                name=s1.getTitle();
            }
        }
        return name;
    }
}
