import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Block extends ImageView {
	public String color;
	public int row;
	
	public Block() {
		// TODO Auto-generated constructor stub
	}

	public Block(String url) {
		super(url);
		// TODO Auto-generated constructor stub
	}
	public Block(Image image, String color, int row) {
		super(image);
		this.color = color;
		this.row = row;
	}

	public Block(Image image) {
		super(image);
		// TODO Auto-generated constructor stub
	}
	
	public String getColor() {
		return color;
	}
	
	public int getRow() {
		return row;
	}
}
