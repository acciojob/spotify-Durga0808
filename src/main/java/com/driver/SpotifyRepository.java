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
        User user=new User();
        user.setMobile(mobile);
        user.setName(name);
        users.add(user);
        return user;
    }

    public Artist createArtist(String name) {
       Artist artist=new Artist();
       artist.setName(name);
       artist.setLikes(0);
       artists.add(artist);
       return artist;
    }

    public Album createAlbum(String title, String artistName) {
        //If the artist does not exist, first create an artist with given name
        //Create an album with given title and artist
        Artist artist=null;
        for(Artist artist1:artists){
            if(artist1.getName().equals(artistName)){
                artist=artist1;
                break;
            }
        }
        if(artist==null){
            artist=createArtist(artistName);
        }
        Album album=new Album();
        album.setTitle(title);
        album.setReleaseDate(new Date());
        albums.add(album);

        List<Album>albumList=artistAlbumMap.getOrDefault(artist,new ArrayList<>());
        albumList.add(album);
        artistAlbumMap.put(artist,albumList);
        return album;
    }

    public Song createSong(String title, String albumName, int length) throws Exception{
        //If the album does not exist in database, throw "Album does not exist" exception
        //Create and add the song to respective album
        Album album=null;
        for( Album album1:albums){
            if(album1.getTitle().equals(albumName)){
                album=album1;
                break;
            }
        }
        if(album==null){
            throw new Exception("Album does not exist");
        }else{
            Song song=new Song();
            song.setTitle(title);
            song.setLength(length);
            song.setLikes(0);
            songs.add(song);

            List<Song>songList=albumSongMap.getOrDefault(album,new ArrayList<>());
            songList.add(song);
            albumSongMap.put(album,songList);

            return song;
        }
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        //Create a playlist with given title and add all songs having the given length in the database to that playlist
        //The creater of the playlist will be the given user and will also be the only listener at the time of playlist creation
        //If the user does not exist, throw "User does not exist" exception

       User user=null;
       for(User user1:users){
           if(user1.getMobile().equals(mobile)){
               user=user1;
               break;
           }
       }
       if(user==null){
           throw new Exception("User does not exist");
       }else{
           Playlist playlist=new Playlist();
           playlist.setTitle(title);
           playlists.add(playlist);
           List<Song>songList=new ArrayList<>();

           for(Song song:songs){
               if(song.getLength()==length){
                   songList.add(song);
               }
           }
           playlistSongMap.put(playlist,songList);
           List<User>userList=new ArrayList<>();
           userList.add(user);
           playlistListenerMap.put(playlist,userList);
           creatorPlaylistMap.put(user,playlist);
           List<Playlist>playlistList=userPlaylistMap.getOrDefault(user,new ArrayList<>());
           playlistList.add(playlist);
           userPlaylistMap.put(user,playlistList);
           return playlist;
       }
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        //Create a playlist with given title and add all songs having the given titles in the database to that playlist
        //The creater of the playlist will be the given user and will also be the only listener at the time of playlist creation
        //If the user does not exist, throw "User does not exist" exception
        User user=null;
        for(User user1:users){
            if(user1.getMobile().equals(mobile)){
                user=user1;
                break;
            }
        }
        if(user==null){
            throw new Exception("User does not exist");
        }

        Playlist playlist=new Playlist();
        playlist.setTitle(title);
        playlists.add(playlist);
        List<Song>songList=new ArrayList<>();

        for(Song song:songs){
            if(songTitles.contains(song.getTitle())){
                songList.add(song);
            }
        }
        playlistSongMap.put(playlist,songList);
        List<User>userList=new ArrayList<>();
        userList.add(user);
        playlistListenerMap.put(playlist,userList);
        creatorPlaylistMap.put(user,playlist);
        List<Playlist>playlistList=userPlaylistMap.getOrDefault(user,new ArrayList<>());
        playlistList.add(playlist);
        userPlaylistMap.put(user,playlistList);
        return playlist;
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        //Find the playlist with given title and add user as listener of that playlist and update user accordingly
        //If the user is creater or already a listener, do nothing
        //If the user does not exist, throw "User does not exist" exception
        //If the playlist does not exists, throw "Playlist does not exist" exception
        // Return the playlist after updating
        User user = null;
        for(User user1:users){
            if(user1.getMobile()==mobile){
                user=user1;
                break;
            }
        }
        if(user==null)
            throw new Exception("User does not exist");

        Playlist playlist = null;
        for(Playlist playlist1:playlists){
            if(playlist1.getTitle().equals(playlistTitle)){
                playlist=playlist1;
                break;
            }
        }
        if(playlist==null)
            throw new Exception("Playlist does not exist");


        if(creatorPlaylistMap.containsKey(user))
            return playlist;

        List<User> listener = playlistListenerMap.get(playlist);
        for(User user1:listener){
            if(user1==user)
                return playlist;
        }

        listener.add(user);
        playlistListenerMap.put(playlist,listener);

        List<Playlist> playlists1 = userPlaylistMap.get(user);
        if(playlists1 == null){
            playlists1 = new ArrayList<>();
        }
        playlists1.add(playlist);
        userPlaylistMap.put(user,playlists1);

        return playlist;

    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
        //The user likes the given song. The corresponding artist of the song gets auto-liked
        //A song can be liked by a user only once. If a user tried to like a song multiple times, do nothing
        //However, an artist can indirectly have multiple likes from a user, if the user has liked multiple songs of that artist.
        //If the user does not exist, throw "User does not exist" exception
        //If the song does not exist, throw "Song does not exist" exception
        //Return the song after updating

        User user=new User();
        boolean userfound=false;

        Song song=new Song();
        boolean songfound=false;

        for(User u1:users){
            if(u1.getMobile().equals(mobile)){
                user=u1;
                userfound=true;
                break;
            }
        }
        if(!userfound){
            throw new Exception("User does not exist");
        }

        for(Song s1:songs){
            if(s1.getTitle().equals(songTitle)){
                song=s1;
                songfound=true;
                break;
            }
        }
        if(!songfound){
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
        int max=Integer.MIN_VALUE;
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
        int max=Integer.MIN_VALUE;;
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
