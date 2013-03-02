package com.automatak.dnp3.tools.controls;

import com.automatak.dnp3.*;
import com.automatak.dnp3.mock.PrintingDataObserver;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CommsTree extends JTree {

    private final static Color darkGreen = new Color(0, 153, 51);

    private class RootNode {
       @Override
       public String toString()
       {
           return "root";
       }
    }

    private abstract class ChannelNode implements ChannelStateListener {

        public Channel getChannel() {
            return channel;
        }

        private final Channel channel;
        private final DefaultTreeModel model;

        public ChannelState getState() {
            return state;
        }

        private ChannelState state = ChannelState.OPEN;

        public ChannelNode(DefaultTreeModel model, Channel channel)
        {
            this.channel = channel;
            this.model = model;
        }

        @Override
        public void onStateChange(final ChannelState state)
        {
            final ChannelNode node = this;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    node.state = state;
                    node.model.reload();
                }
            });
        }
    }

    private abstract class StackNode implements StackStateListener {

        public Stack getStack() {
            return stack;
        }

        private final Stack stack;
        private final DefaultTreeModel model;

        public StackState getState() {
            return state;
        }

        private StackState state = StackState.COMMS_DOWN;

        public StackNode(DefaultTreeModel model, Stack stack)
        {
            this.stack = stack;
            this.model = model;
        }

        // overridable to close windows, etc
        protected void cleanup()
        {}

        @Override
        public void onStateChange(final StackState state)
        {
            final StackNode node = this;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    node.state = state;
                    node.model.reload();
                }
            });
        }
    }

    private class TcpClientChannelNode extends ChannelNode {

        private final String id;
        private final String host;
        private final int port;

        public TcpClientChannelNode(DefaultTreeModel model, String id, String host, int port, Channel channel)
        {
            super(model, channel);
            this.id = id;
            this.host = host;
            this.port = port;
        }

        @Override
        public String toString()
        {
            return host + ":" + port;
        }
    }

    private class MasterNode extends StackNode {

        public Master getMaster() {
            return master;
        }

        public MasterForm getForm() {
            return form;
        }

        private final Master master;
        private final MasterForm form;

        @Override
        protected void cleanup()
        {
            this.form.setVisible(false);
        }

        public MasterNode(DefaultTreeModel model, Master master, MasterForm form)
        {
            super(model, master);
            this.master = master;
            this.form = form;
        }

        @Override
        public String toString()
        {
            return "master";
        }
    }

    private final DefaultMutableTreeNode root = new DefaultMutableTreeNode(new RootNode());
    private final DefaultTreeModel model = new DefaultTreeModel(root);
    private final JPopupMenu rootMenu = getRootMenu();


    private class MyCellRenderer extends DefaultTreeCellRenderer {

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean exp, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, exp, leaf, row, hasFocus);
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object userObject = node.getUserObject();

            if(RootNode.class.isInstance(userObject)) {

            }
            else if(ChannelNode.class.isInstance(userObject)) {
                ChannelNode cnode = (ChannelNode) userObject;
                switch(cnode.getState())
                {
                    case OPENING:
                        setForeground(Color.ORANGE);
                        break;
                    case OPEN:
                        setForeground(darkGreen);
                        break;
                    default:
                        setForeground(Color.BLACK);
                        break;
                }
            } else if(StackNode.class.isInstance(userObject)) {
                StackNode snode = (StackNode) userObject;
                switch(snode.getState())
                {
                    case COMMS_UP:
                        setForeground(darkGreen);
                        break;
                    default:
                        setForeground(Color.RED);
                        break;
                }
            }

            return this;
        }
    }

    private ImageIcon createImageIcon(String path, String description) {
        java.net.URL imgURL = getClass().getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL, description);
        } else {
            throw new RuntimeException("Couldn't find file: " + path);
        }
    }

    public void setManager(DNP3Manager manager) {
        this.manager = manager;
    }

    private DNP3Manager manager = null;

    private JPopupMenu getStackMenu(final DefaultMutableTreeNode node, final StackNode snode)
    {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem removeItem = new JMenuItem("Remove");
        removeItem.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                snode.cleanup();
                snode.getStack().shutdown();
                node.removeFromParent();
                model.reload();
            }
        });
        popup.add(removeItem);
        return popup;
    }

    private JPopupMenu getChannelMenu(final DefaultMutableTreeNode node, final ChannelNode cnode)
    {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem addMasterItem = new JMenuItem("Add Master");
        addMasterItem.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
               AddMasterDialog dialog = new AddMasterDialog(new AddMasterListener() {
                   @Override
                   public void onAdd(MasterStackConfig config) {
                       Channel c = cnode.getChannel();
                       MasterForm form = new MasterForm("master");
                       Master m = c.addMaster("master", LogLevel.INTERPRET, form, config);
                       form.configureWithMaster(m);
                       MasterNode mnode = new MasterNode(model, m, form);
                       m.addStateListener(mnode);
                       DefaultMutableTreeNode child = new DefaultMutableTreeNode(mnode);
                       node.add(child);
                       model.reload();
                   }
               });
               dialog.pack();
               dialog.setVisible(true);
            }
        });
        popup.add(addMasterItem);
        popup.add(new JPopupMenu.Separator());
        JMenuItem removeItem = new JMenuItem("Remove");
        removeItem.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Channel c = cnode.getChannel();
                c.shutdown();
                node.removeFromParent();
                model.reload();
            }
        });
        popup.add(removeItem);
        return popup;
    }

    private JPopupMenu getRootMenu()
    {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem addClientItem = new JMenuItem("Add Tcp Client");
        addClientItem.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if(SwingUtilities.isLeftMouseButton(e))
                {
                    AddTcpDialog dialog = new AddTcpDialog("Add Tcp Client", "IP", new AddTcpListener() {
                        @Override
                        public void onAdd(String loggerId, LogLevel level, int retryMs, String host, int port)
                        {
                            Channel c = manager.addTCPClient(loggerId, level, retryMs, host, port);
                            TcpClientChannelNode node = new TcpClientChannelNode(model, loggerId, host, port, c);
                            c.addStateListener(node);
                            MutableTreeNode channelNode = new DefaultMutableTreeNode(node);
                            DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
                            root.add(channelNode);
                            model.reload();
                        }
                    });
                    dialog.pack();
                    dialog.setVisible(true);
                }
            }
        });
        popup.add(addClientItem);
        return popup;
    }

    public CommsTree()
    {
        this.setModel(model);
        this.setCellRenderer(new MyCellRenderer());

        final JTree tree = this;

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();
                int row = tree.getRowForLocation(e.getX(), e.getY());
                TreePath path = tree.getPathForRow(row);
                if(path != null)
                {
                    final DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                    if(SwingUtilities.isRightMouseButton(e))
                    {
                        if(RootNode.class.isInstance(node.getUserObject()))
                        {
                            rootMenu.show(tree, x, y);
                        } else if(ChannelNode.class.isInstance(node.getUserObject())) {
                            ChannelNode cnode = (ChannelNode) node.getUserObject();
                            getChannelMenu(node, cnode).show(tree, x, y);
                        } else if(StackNode.class.isInstance(node.getUserObject())) {
                            StackNode snode = (StackNode) node.getUserObject();
                            getStackMenu(node, snode).show(tree, x, y);
                        }
                    }
                    else if(SwingUtilities.isLeftMouseButton(e))
                    {
                        if(MasterNode.class.isInstance(node.getUserObject()) && e.getClickCount() == 2)
                        {
                            MasterNode mnode = (MasterNode) node.getUserObject();
                            mnode.getForm().setVisible(true);
                        }
                    }
                }
            }
        });

    }


}