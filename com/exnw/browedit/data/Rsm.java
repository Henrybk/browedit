package com.exnw.browedit.data;

import com.exnw.browedit.math.Quaternion;

public class Rsm{
	private static byte[] magic = "GRSM".getBytes();
	private static byte[][] supportedVersions = new byte[][]{
		{ 1, 1 },
		{ 1, 2 },
		{ 1, 3 },
		{ 1, 4 },
		{ 1, 5 }
	};
	
	private String filename;
	private byte version_major;
	private byte version_minor;
	
	private int animationlength;
	private int shadetype;
	private byte alpha;
	
	private java.util.List<String> textures;
	private String root;
	private java.util.List<Rsm.RsmMesh> meshes;
	
	public Rsm( String filename ){
		if( filename == null || filename.isEmpty() )
			throw new IllegalArgumentException("No empty filename allowed.");
		
		filename = filename.trim();
		
		if( !filename.endsWith(".rsm") )
			filename += ".rsm";
		
		this.filename = filename;
		this.read();
	}
	
	public static boolean isSupported( byte major, byte minor ){
		for( byte[]version : Rsm.supportedVersions ){
			if( major == version[0] && minor == version[1] ){
				return true;
			}
		}
		return false;
	}
	
	public void read(){
		com.exnw.browedit.io.SwappedInputStream dis = null;
		
		try{
			dis = new com.exnw.browedit.io.SwappedInputStream( com.exnw.browedit.grflib.GrfLib.openFile( this.filename ) );
			
			for( byte b : Rsm.magic ){
				if( b != dis.readByte() ){
					throw new IllegalArgumentException("RSM file header is corrupted.");
				}
			}
			
			this.version_major = dis.readByte();
			this.version_minor = dis.readByte();
			
			if( !Rsm.isSupported( this.version_major, this.version_minor ) )
				throw new IllegalArgumentException( String.format( "RSM Version %01d.%01d not supported.", this.version_major, this.version_minor ) );
			
			this.animationlength = dis.readInt();
			this.shadetype = dis.readInt();
			
			if( this.version_minor >= 4 ){ // Since RSM 1.4
				this.alpha = dis.readByte();
			}else{
				this.alpha = (byte)0xFF; // TODO: Check this, since you use 0 here.
			}
			
			dis.skip( 16 );
			
			this.setTextures(new java.util.ArrayList<String>());
			
			for( int i = 0, count = dis.readInt(); i < count; i++ ){
				this.getTextures().add( dis.readISOString(40) );
			}
			
			{
				this.setRoot(dis.readISOString(40));
			}
			
			this.setMeshes(new java.util.ArrayList<Rsm.RsmMesh>());
			for( int i = 0, count = dis.readInt(); i < count; i++ ){
				Rsm.RsmMesh mesh = new Rsm.RsmMesh();
				mesh.read( dis );
				this.getMeshes().add( mesh );
			}
			
/*			if( this.version_minor < 5 ){
				Rsm.RsmMesh mesh = this.getMeshes().get(0);
				
				mesh.positionframes = new java.util.ArrayList<Rsm.RsmMesh.PositionFrame>();
				
				for( int i = 0, count = dis.readInt(); i < count; i++ ){
					Rsm.RsmMesh.PositionFrame pf = mesh.new PositionFrame();
					
					pf.frame = dis.readInt();
					
					pf.position = new com.exnw.browedit.math.Vector4(dis);
					
					mesh.positionframes.add(pf);
				}
			}*/
		}catch( java.io.IOException ex ){
			ex.printStackTrace();
		}finally{
			if( dis != null ){
				try{
					dis.close();
				}catch( java.io.IOException ex ){
					ex.printStackTrace();
				}
			}
		}
	}
	
	public void setMeshes(java.util.List<Rsm.RsmMesh> meshes)
	{
		this.meshes = meshes;
	}

	public java.util.List<Rsm.RsmMesh> getMeshes()
	{
		return meshes;
	}

	public void setRoot(String root)
	{
		this.root = root;
	}

	public String getRoot()
	{
		return root;
	}

	public void setTextures(java.util.List<String> textures)
	{
		this.textures = textures;
	}

	public java.util.List<String> getTextures()
	{
		return textures;
	}

	public class RsmMesh{
		private String name;
		private String parent;
		private java.util.List<Integer> textureids;
		
		private com.exnw.browedit.math.Matrix4 matrix;
		private com.exnw.browedit.math.Vector3 position;
		private com.exnw.browedit.math.Vector3 position2;

		private float rotationangle;
		private com.exnw.browedit.math.Vector3 rotationaxis;
		private com.exnw.browedit.math.Vector3 scale;
		
		private java.util.List<com.exnw.browedit.math.Vector3> vertices;
		private java.util.List<Rsm.RsmMesh.TextureCoordinate> textureCoordinats;
		private java.util.List<Rsm.RsmMesh.Surface> surfaces;
		private java.util.List<Rsm.RsmMesh.AnimationFrame> animationFrames;
		
		public RsmMesh(){
			
		}
		
		public void read( com.exnw.browedit.io.SwappedInputStream in ) throws java.io.IOException{
			this.setName(in.readISOString(40));
			this.setParent(in.readISOString(40));
			
			this.setTextureids(new java.util.ArrayList<Integer>());			
			for( int i = 0, count = in.readInt(); i < count; i++ ){
				this.getTextureids().add( in.readInt() );
			}
			
			this.setMatrix(new com.exnw.browedit.math.Matrix4(in));
			
			this.setPosition(new com.exnw.browedit.math.Vector3(in));
			
			this.position2 = new com.exnw.browedit.math.Vector3(in);
			
			this.setRotationangle(in.readFloat());
			
			this.setRotationaxis(new com.exnw.browedit.math.Vector3(in));
			
			this.setScale(new com.exnw.browedit.math.Vector3(in));
			
			this.setVertices(new java.util.ArrayList<com.exnw.browedit.math.Vector3>());
			for( int i = 0, count = in.readInt(); i < count; i++ ){				
				this.getVertices().add( new com.exnw.browedit.math.Vector3(in) );
			}
			
			this.setTextureCoordinats(new java.util.ArrayList<Rsm.RsmMesh.TextureCoordinate>());
			for( int i = 0, count = in.readInt(); i < count; i++ ){
				Rsm.RsmMesh.TextureCoordinate tv = new Rsm.RsmMesh.TextureCoordinate();
				
				if( Rsm.this.version_minor >= 2 )
					tv.color = new java.awt.Color( in.readInt() );
				else
					tv.color = new java.awt.Color( 0xFFFFFFFF );
				
				tv.setCoodinates(new com.exnw.browedit.math.Vector2(in));
				
				this.getTextureCoordinats().add( tv );
			}
			
			this.setSurfaces(new java.util.ArrayList<Rsm.RsmMesh.Surface>());
			for( int i = 0, count = in.readInt(); i < count; i++ ){
				Rsm.RsmMesh.Surface s = new Rsm.RsmMesh.Surface();
				
				for( int j = 0; j < 3; j++ )
					s.getSurfacevertices()[j] = in.readShort();
				
				for( int j = 0; j < 3; j++ )
					s.getTexturevertices()[j] = in.readShort();
				
				s.setTextureid(in.readShort());
				s.padding = in.readShort();
				s.twoside = in.readInt();
				
				if( Rsm.this.version_minor >= 2 ){
					s.smoothgroup = in.readInt();
				}else{
					s.smoothgroup = 0;
				}
				
				this.getSurfaces().add( s );
			}
			
			this.setAnimationFrames(new java.util.ArrayList<Rsm.RsmMesh.AnimationFrame>());
			for( int i = 0, count = in.readInt(); i < count; i++ ){
				Rsm.RsmMesh.AnimationFrame rf = new Rsm.RsmMesh.AnimationFrame();
				
				rf.setTime(in.readInt());
				rf.setQuat(new com.exnw.browedit.math.Quaternion(in));
				
				this.getAnimationFrames().add(rf);
			}
		}
		
		public void setParent(String parent)
		{
			this.parent = parent;
		}

		public String getParent()
		{
			return parent;
		}

		public void setName(String name)
		{
			this.name = name;
		}

		public String getName()
		{
			return name;
		}

		public void setSurfaces(java.util.List<Rsm.RsmMesh.Surface> surfaces)
		{
			this.surfaces = surfaces;
		}

		public java.util.List<Rsm.RsmMesh.Surface> getSurfaces()
		{
			return surfaces;
		}

		public void setVertices(java.util.List<com.exnw.browedit.math.Vector3> vertices)
		{
			this.vertices = vertices;
		}

		public java.util.List<com.exnw.browedit.math.Vector3> getVertices()
		{
			return vertices;
		}

		public void setPosition(com.exnw.browedit.math.Vector3 position)
		{
			this.position = position;
		}

		public com.exnw.browedit.math.Vector3 getPosition()
		{
			return position;
		}
		
		public com.exnw.browedit.math.Vector3 getPosition2()
		{
			return position2;
		}

		public void setPosition2(com.exnw.browedit.math.Vector3 position2)
		{
			this.position2 = position2;
		}

		public void setRotationangle(float rotationangle)
		{
			this.rotationangle = rotationangle;
		}

		public float getRotationangle()
		{
			return rotationangle;
		}

		public void setRotationaxis(com.exnw.browedit.math.Vector3 rotationaxis)
		{
			this.rotationaxis = rotationaxis;
		}

		public com.exnw.browedit.math.Vector3 getRotationaxis()
		{
			return rotationaxis;
		}

		public void setScale(com.exnw.browedit.math.Vector3 scale)
		{
			this.scale = scale;
		}

		public com.exnw.browedit.math.Vector3 getScale()
		{
			return scale;
		}

		public void setMatrix(com.exnw.browedit.math.Matrix4 matrix)
		{
			this.matrix = matrix;
		}

		public com.exnw.browedit.math.Matrix4 getMatrix()
		{
			return matrix;
		}

		public void setTextureCoordinats(java.util.List<Rsm.RsmMesh.TextureCoordinate> textureCoordinats)
		{
			this.textureCoordinats = textureCoordinats;
		}

		public java.util.List<Rsm.RsmMesh.TextureCoordinate> getTextureCoordinats()
		{
			return textureCoordinats;
		}

		public void setTextureids(java.util.List<Integer> textureids)
		{
			this.textureids = textureids;
		}

		public java.util.List<Integer> getTextureids()
		{
			return textureids;
		}

		public void setAnimationFrames(java.util.List<Rsm.RsmMesh.AnimationFrame> animationFrames)
		{
			this.animationFrames = animationFrames;
		}

		public java.util.List<Rsm.RsmMesh.AnimationFrame> getAnimationFrames()
		{
			return animationFrames;
		}

		public class AnimationFrame
		{
			private int time;
			private Quaternion quat;
			public void setQuat(Quaternion quat)
			{
				this.quat = quat;
			}
			public Quaternion getQuat()
			{
				return quat;
			}
			public void setTime(int time)
			{
				this.time = time;
			}
			public int getTime()
			{
				return time;
			}

		}

		private class RotationFrame{
			private int frame;
		}
		
		private class PositionFrame{
			private int frame;
		}
		
		public class Surface{
			private short[] surfacevertices = new short[3];
			private short[] texturevertices = new short[3];
			private short textureid;
			private short padding;
			private int twoside;
			private int smoothgroup;
			public void setSurfacevertices(short[] surfacevertices)
			{
				this.surfacevertices = surfacevertices;
			}
			public short[] getSurfacevertices()
			{
				return surfacevertices;
			}
			public void setTexturevertices(short[] texturevertices)
			{
				this.texturevertices = texturevertices;
			}
			public short[] getTexturevertices()
			{
				return texturevertices;
			}
			public void setTextureid(short textureid)
			{
				this.textureid = textureid;
			}
			public short getTextureid()
			{
				return textureid;
			}
		}
		
		public class TextureCoordinate{
			private com.exnw.browedit.math.Vector2 coodinates;
			private java.awt.Color color;
			public void setCoodinates(com.exnw.browedit.math.Vector2 coodinates)
			{
				this.coodinates = coodinates;
			}
			public com.exnw.browedit.math.Vector2 getCoodinates()
			{
				return coodinates;
			}
		}
	}
}
