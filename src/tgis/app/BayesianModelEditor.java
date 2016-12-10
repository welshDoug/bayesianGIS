package tgis.app;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.event.KeyEvent;
import java.awt.Color;
import java.awt.SystemColor;
import javax.swing.Action;

import tgis.datatypes.LinkType;
import tgis.datatypes.ModelElement;
import tgis.datatypes.Node;
import tgis.datatypes.RawDate;

import java.beans.PropertyChangeListener;
import java.lang.Object;
import java.lang.String;

/**
 * Limited GUI for building up OxCal type model, for use with the TGIS.
 * So far this is a shell UI, with limited functionality, it will take a lot more to complete it.
 * 
 * Development halted because this isnt a key priority for my research.
 */
public class BayesianModelEditor extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel jContentPane;
	private JList<String> ctxList = null;
	//  @jve:decl-index=0:
	private JPanel modelPanel = null;
	private JPanel buttonPanel = null;
	private JTree modelTree = null;
	private JButton seqBtn = null;
	private JButton phaBtn = null;
	private JList<String> dateList = null;
	private JMenuBar jJMenuBar = null;
	private JMenu actionsMenu = null;
	private JMenu kwordsMenu = null;
	private JMenuItem genMenuItem = null;


	/**
	 * This method initializes ctxList	
	 * 	
	 * @return javax.swing.JList	
	 */
	private JList<String> getCtxList() {
		if (ctxList == null) {
			DefaultListModel<String> ctxListModel = new DefaultListModel<String>();
			ctxListModel.addElement("event1");
			ctxListModel.addElement("event2");
			ctxListModel.addElement("event3");
			ctxList = new JList<String>();
			ctxList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			ctxList.setModel(ctxListModel);
			ctxList.setDragEnabled(true);
		}
		return ctxList;
	}

	/**
	 * This method initializes modelPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getModelPanel() {
		if (modelPanel == null) {
			modelPanel = new JPanel();
			modelPanel.setLayout(new BorderLayout());
			modelPanel.add(getButtonPanel(), BorderLayout.NORTH);
			modelPanel.add(getModelTree(), BorderLayout.CENTER);
		}
		return modelPanel;
	}

	/**
	 * This method initializes buttonPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			buttonPanel = new JPanel();
			buttonPanel.setLayout(new FlowLayout());
			buttonPanel.add(getSeqBtn(), null);
			buttonPanel.add(getPhaBtn(), null);
		}
		return buttonPanel;
	}

	/**
	 * This method initializes modelTree	
	 * 	
	 * @return javax.swing.JTree	
	 */
	private JTree getModelTree() {
		if (modelTree == null) {
			DefaultTreeModel treeModel = new DefaultTreeModel(new DefaultMutableTreeNode("Sequence"));
			modelTree = new JTree();
			modelTree.setModel(treeModel);
			modelTree.setDropMode(DropMode.ON_OR_INSERT);
			modelTree.setDragEnabled(true);
			modelTree.getSelectionModel().setSelectionMode(
	                TreeSelectionModel.SINGLE_TREE_SELECTION);
			modelTree.setTransferHandler(new TreeTransferHandler(modelTree, ctxList, dateList));
		}
		return modelTree;
	}

	/**
	 * This method initializes seqBtn	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getSeqBtn() {
		if (seqBtn == null) {
			seqBtn = new JButton();
			seqBtn.setText("Sequence");
			seqBtn.setBackground(SystemColor.control);
			seqBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					DefaultMutableTreeNode newNode =
			            new DefaultMutableTreeNode("Sequence");
					DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) modelTree.getModel().getRoot();
					((DefaultTreeModel) modelTree.getModel()).insertNodeInto(newNode, parentNode, parentNode.getChildCount());
				}
			});
		}
		return seqBtn;
	}

	/**
	 * This method initializes phaBtn	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getPhaBtn() {
		if (phaBtn == null) {
			phaBtn = new JButton();
			phaBtn.setText("Phase");
			phaBtn.setBackground(SystemColor.control);
			phaBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					DefaultMutableTreeNode newNode =
			            new DefaultMutableTreeNode("Phase");
			    DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) modelTree.getModel().getRoot();
			    ((DefaultTreeModel) modelTree.getModel()).insertNodeInto(newNode, parentNode, parentNode.getChildCount());
				}
			});

		}
		return phaBtn;
	}

	/**
	 * This method initializes dateList	
	 * 	
	 * @return javax.swing.JList	
	 */
	private JList<String> getDateList() {
		if (dateList == null) {
			DefaultListModel<String> dateListModel = new DefaultListModel<String>();
			dateListModel.addElement("date1");
			dateListModel.addElement("date2");
			dateListModel.addElement("date3");
			dateList = new JList<String>();
			dateList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			dateList.setModel(dateListModel);
			dateList.setDragEnabled(true);
		}
		return dateList;
	}

	/**
	 * This method initializes jJMenuBar	
	 * 	
	 * @return javax.swing.JMenuBar	
	 */
	private JMenuBar getJJMenuBar() {
		if (jJMenuBar == null) {
			jJMenuBar = new JMenuBar();
			jJMenuBar.setBackground(SystemColor.control);
			jJMenuBar.add(getActionsMenu());
			jJMenuBar.add(getKwordsMenu());
		}
		return jJMenuBar;
	}

	/**
	 * This method initializes actionsMenu	
	 * 	
	 * @return javax.swing.JMenu	
	 */
	private JMenu getActionsMenu() {
		if (actionsMenu == null) {
			actionsMenu = new JMenu();
			actionsMenu.setName("Actions");
			actionsMenu.setMnemonic(KeyEvent.VK_A);
			actionsMenu.setText("Actions");
			actionsMenu.setActionCommand("");
			actionsMenu.add(getGenMenuItem());
		}
		return actionsMenu;
	}

	/**
	 * This method initializes kwordsMenu	
	 * 	
	 * @return javax.swing.JMenu	
	 */
	private JMenu getKwordsMenu() {
		if (kwordsMenu == null) {
			kwordsMenu = new JMenu();
			kwordsMenu.setText("Keywords");
		}
		return kwordsMenu;
	}

	/**
	 * This method initializes genMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getGenMenuItem() {
		if (genMenuItem == null) {
			genMenuItem = new JMenuItem();
			genMenuItem.setAction(new AbstractAction(){
				public void actionPerformed(ActionEvent arg0) {
					DefaultMutableTreeNode root = (DefaultMutableTreeNode) modelTree.getModel().getRoot();
					ModelElement rootNode = convertTreeNodeToModel(root);
					
					StringBuilder oxCalText = new StringBuilder();
					rootNode.writeToOxCal(oxCalText);
					
					System.out.println(oxCalText.toString());
				}});
			genMenuItem.setText("Generate");
		}
		return genMenuItem;
	}
	
	private ModelElement convertTreeNodeToModel(TreeNode obj) {
		String text = obj.toString();
		
		ModelElement node;
		if (text.equals("Sequence")) {
			node = new Node("", LinkType.SEQUENCE);
		}
		else if (text.equals("Phase")) {
			node = new Node("", LinkType.PHASE);
		}
		else {
			//Leaf node
			node = new RawDate("", 0, 0);
		}
		
		if ((obj.getChildCount() > 0) && (node instanceof Node)) {
			for (int i = 0; i < obj.getChildCount(); i++) {
				TreeNode child = obj.getChildAt(i);
				
				ModelElement childNode = convertTreeNodeToModel(child);
				((Node) node).getChildren().add(childNode);
			}
		}
		
		return node;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				BayesianModelEditor thisClass = new BayesianModelEditor();
				thisClass.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				thisClass.setVisible(true);
			}
		});
	}

	/**
	 * This is the default constructor
	 */
	public BayesianModelEditor() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(533, 357);
		this.setJMenuBar(getJJMenuBar());
		this.setContentPane(getJContentPane());
		this.setTitle("Bayesian Model Editor");
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			GridLayout gridLayout = new GridLayout();
			gridLayout.setRows(1);
			gridLayout.setHgap(5);
			gridLayout.setColumns(3);
			jContentPane = new JPanel();
			jContentPane.setLayout(gridLayout);
			jContentPane.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
			getCtxList();
			getDateList();
			jContentPane.add(getModelPanel(), null);
			jContentPane.add(ctxList, null);
			jContentPane.add(dateList, null);
		}
		return jContentPane;
	}


}  //  @jve:decl-index=0:visual-constraint="7,2"

@SuppressWarnings("serial")
class TreeTransferHandler extends TransferHandler {
    JTree modelTree;
    JList<String> ctxList;
    JList<String> dateList;
    DataFlavor nodesFlavor;
    DataFlavor[] flavors = new DataFlavor[1];
    DefaultMutableTreeNode[] nodesToRemove;
    
	public TreeTransferHandler(JTree modelTree, JList<String> ctxList, JList<String> dateList) {
		this.modelTree = modelTree;
		this.ctxList = ctxList;
		this.dateList = dateList;
		
        try {
            String mimeType = DataFlavor.javaJVMLocalObjectMimeType +
                              ";class=\"" +
                javax.swing.tree.DefaultMutableTreeNode[].class.getName() +
                              "\"";
            nodesFlavor = new DataFlavor(mimeType);
            flavors[0] = nodesFlavor;
        } catch(ClassNotFoundException e) {
            System.out.println("ClassNotFound: " + e.getMessage());
        }
	}
	
	public boolean canImport(TransferSupport support) {	
		if (!support.isDrop()) {
			return false;
		}
		
	    if (support.isDataFlavorSupported(DataFlavor.stringFlavor)) {
	        return true;
	    }
	    
		if (nodesFlavor == null) {
			return false;
		}
	    if (!support.isDataFlavorSupported(nodesFlavor)) {
	    	return false;
	    }
	    // Do not allow a drop on the drag source selections.
        JTree.DropLocation dl =
                (JTree.DropLocation)support.getDropLocation();
        JTree tree = (JTree)support.getComponent();
        int dropRow = tree.getRowForPath(dl.getPath());
        int[] selRows = tree.getSelectionRows();
        for(int i = 0; i < selRows.length; i++) {
            if(selRows[i] == dropRow) {
                return false;
            }
        }

	    return true;
	}
	
	public int getSourceActions(JComponent c) {
        return MOVE;
    }
	
	public boolean importData(TransferSupport support) {
	    if (!canImport(support)) {
	        return false;
	    }

	    Transferable transferable = support.getTransferable();

	    if (support.isDataFlavorSupported(DataFlavor.stringFlavor)) {
		    try {
		    	String transferData = (String)transferable.getTransferData(DataFlavor.stringFlavor);
		    	doStringTransfer(support, transferData);
		    } catch (IOException e) {
		        return false;
		    } catch (UnsupportedFlavorException e) {
		        return false;
		    }
	    }
	    else if (support.isDataFlavorSupported(nodesFlavor)) {
	        try {
	            DefaultMutableTreeNode[] nodes = (DefaultMutableTreeNode[])transferable.getTransferData(nodesFlavor);
	            doNodesTransfer(support, nodes);
	        } catch(UnsupportedFlavorException ufe) {
	            System.out.println("UnsupportedFlavor: " + ufe.getMessage());
	        } catch(java.io.IOException ioe) {
	            System.out.println("I/O error: " + ioe.getMessage());
	        }
	    }
	    
	    return true;
	}
	
	private void doStringTransfer(TransferSupport support, String transferData) {
		JTree.DropLocation dropLocation =
            (JTree.DropLocation)support.getDropLocation();
		TreePath path = dropLocation.getPath();
    
	    int childIndex = dropLocation.getChildIndex();

		if (childIndex == -1) {
	        childIndex = modelTree.getModel().getChildCount(path.getLastPathComponent());
	    }

	    DefaultMutableTreeNode newNode =
	            new DefaultMutableTreeNode(transferData);
	    DefaultMutableTreeNode parentNode =
	            (DefaultMutableTreeNode)path.getLastPathComponent();
	    ((DefaultTreeModel) modelTree.getModel()).insertNodeInto(newNode, parentNode, childIndex);

	    TreePath newPath = path.pathByAddingChild(newNode);
	    modelTree.makeVisible(newPath);
	    modelTree.scrollRectToVisible(modelTree.getPathBounds(newPath));

		if (ctxList.getSelectedIndex() > -1) {
			((DefaultListModel<String>) ctxList.getModel()).remove(ctxList.getSelectedIndex());
	    }
	    else {
			((DefaultListModel<String>) dateList.getModel()).remove(dateList.getSelectedIndex());
	    }
	}
	
	private void doNodesTransfer(TransferSupport support, DefaultMutableTreeNode[] nodes) {
	    JTree.DropLocation dl =
               (JTree.DropLocation)support.getDropLocation();
       int childIndex = dl.getChildIndex();
       TreePath dest = dl.getPath();
       DefaultMutableTreeNode parent =
           (DefaultMutableTreeNode)dest.getLastPathComponent();
       JTree tree = (JTree)support.getComponent();
       DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
       // Configure for drop mode.
       int index = childIndex;    // DropMode.INSERT
       if(childIndex == -1) {     // DropMode.ON
           index = parent.getChildCount();
       }
       // Add data to model.
       for(int i = 0; i < nodes.length; i++) {
           model.insertNodeInto(nodes[i], parent, index++);
       }
	}
	
	protected Transferable createTransferable(JComponent c) {
        JTree tree = (JTree)c;
        TreePath[] paths = tree.getSelectionPaths();
        if(paths != null) {
            // Make up a node array of copies for transfer and
            // another for/of the nodes that will be removed in
            // exportDone after a successful drop.
            List<DefaultMutableTreeNode> copies =
                new ArrayList<DefaultMutableTreeNode>();
            List<DefaultMutableTreeNode> toRemove =
                new ArrayList<DefaultMutableTreeNode>();
            DefaultMutableTreeNode node =
                (DefaultMutableTreeNode)paths[0].getLastPathComponent();
            DefaultMutableTreeNode copy = copy(node);
            copies.add(copy);
            toRemove.add(node);
            for(int i = 1; i < paths.length; i++) {
                DefaultMutableTreeNode next =
                    (DefaultMutableTreeNode)paths[i].getLastPathComponent();
                // Do not allow higher level nodes to be added to list.
                if(next.getLevel() < node.getLevel()) {
                    break;
                } else if(next.getLevel() > node.getLevel()) {  // child node
                    copy.add(copy(next));
                    // node already contains child
                } else {                                        // sibling
                    copies.add(copy(next));
                    toRemove.add(next);
                }
            }
            DefaultMutableTreeNode[] nodes =
                copies.toArray(new DefaultMutableTreeNode[copies.size()]);
            nodesToRemove = toRemove.toArray(new DefaultMutableTreeNode[toRemove.size()]);
            return new NodesTransferable(nodes);
        }
        return null;
	}
	
	private DefaultMutableTreeNode copy(TreeNode node) {
		return new DefaultMutableTreeNode(node);
	}
	
    protected void exportDone(JComponent source, Transferable data, int action) {
        if((action & MOVE) == MOVE) {
            JTree tree = (JTree)source;
            DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
            // Remove nodes saved in nodesToRemove in createTransferable.
            for(int i = 0; i < nodesToRemove.length; i++) {
                model.removeNodeFromParent(nodesToRemove[i]);
            }
        }
    }
	
    public class NodesTransferable implements Transferable {
        DefaultMutableTreeNode[] nodes;

        public NodesTransferable(DefaultMutableTreeNode[] nodes) {
            this.nodes = nodes;
         }

        public Object getTransferData(DataFlavor flavor)
                                 throws UnsupportedFlavorException {
            if(!isDataFlavorSupported(flavor))
                throw new UnsupportedFlavorException(flavor);
            return nodes;
        }

        public DataFlavor[] getTransferDataFlavors() {
            return flavors;
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return nodesFlavor.equals(flavor);
        }
    }
}

/**
 * TODO
 * create custom tree to work with my datatypes, provide restrictions, checking etc
 * make seq/pha text editable + boundaries
 * ponder on boundaries, tpq, etc
 * -make ui so far prittier and more usable
 * -tie this into larger application
 * 
 * Application structure:
 * Main Page
 * -load shapefiles, into TOC control, show on map
 * -load dates, link to events in shapefiles
 * -launch modeller, with 1 shapefile (allow creating & using multiple models)
 * -review generated oxcal input and send to oxcal
 * -load results from oxcal, put into data structure linking to contexts via model
 * (will need user interaction to decide which date to use per context - or can use start, end bits, etc)
 */