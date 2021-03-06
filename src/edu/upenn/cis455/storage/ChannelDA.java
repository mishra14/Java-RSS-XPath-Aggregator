package edu.upenn.cis455.storage;

import java.util.ArrayList;
import java.util.HashSet;

import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.PrimaryIndex;
import edu.upenn.cis455.bean.Channel;
import edu.upenn.cis455.xpath.XPath;

/**
 * Data Accessor class for the channel entity class
 * 
 * @author cis455
 *
 */
public class ChannelDA
{

	public static Channel getChannel(String channelName) // returns null if the
															// entry
	// wasn't found
	{
		Channel channel = null;
		if (DBWrapper.getStore() != null)
		{
			PrimaryIndex<String, Channel> channelPrimaryIndex = DBWrapper
					.getStore().getPrimaryIndex(String.class, Channel.class);
			if (channelPrimaryIndex != null)
			{
				channel = channelPrimaryIndex.get(channelName);
			}
		}
		return channel;
	}

	public static Channel putChannel(Channel channel) // returns null is the
														// channel did not
	// exist in the DB
	{
		Channel insertedChannel = null;
		if (DBWrapper.getStore() != null)
		{
			HashSet<String> xPaths = new HashSet<String>();
			for (String xPathString : channel.getxPaths())
			{
				XPath newXPath = new XPath(xPathString);
				if (newXPath.isValid())
				{
					XPath xPath = XPathDA.getXPath(newXPath.getxPath());
					if (xPath == null)
					{
						xPath = newXPath;
					}
					xPath.getChannelNames().add(channel.getChannelName());
					XPathDA.putXPath(xPath);
					xPaths.add(xPath.getxPath());
				}
			}
			channel.setxPaths(xPaths);
			PrimaryIndex<String, Channel> channelPrimaryIndex = DBWrapper
					.getStore().getPrimaryIndex(String.class, Channel.class);
			if (channelPrimaryIndex != null)
			{
				insertedChannel = channelPrimaryIndex.put(channel);
			}
		}
		return insertedChannel;
	}

	public static boolean removeChannel(String channelName)
	{
		PrimaryIndex<String, Channel> channelPrimaryIndex = DBWrapper
				.getStore().getPrimaryIndex(String.class, Channel.class);
		if (channelPrimaryIndex != null && getChannel(channelName) != null)
		{
			Channel channel = getChannel(channelName);
			boolean result = channelPrimaryIndex.delete(channelName);
			for (String xPathString : channel.getxPaths())
			{
				XPath xPath = XPathDA.getXPath(xPathString);
				if (xPath != null)
				{
					xPath.getChannelNames().remove(channelName);
					if (xPath.getChannelNames().isEmpty())
					{
						XPathDA.removeXPath(xPathString);
					}
					else
					{
						XPathDA.putXPath(xPath);
					}
				}
			}
			return result;
		}
		return false;
	}

	public static ArrayList<Channel> getAllChannels()
	{
		ArrayList<Channel> result = null;
		if (DBWrapper.getStore() != null)
		{
			PrimaryIndex<String, Channel> channelPrimaryIndex = DBWrapper
					.getStore().getPrimaryIndex(String.class, Channel.class);
			if (channelPrimaryIndex != null)
			{
				result = new ArrayList<Channel>();
				EntityCursor<Channel> channelCursor = channelPrimaryIndex
						.entities();
				try
				{
					for (Channel channel : channelCursor)
					{
						result.add(channel);
					}

				}
				finally
				{
					channelCursor.close();
				}
			}

		}

		return result;
	}
	/*public static void main(String args[]) throws Exception
	{
		DBWrapper.openDBWrapper("./db/");
		User user = new User("tess","t");
		UserDA.putUser(user);
		Channel channel = new Channel("peace","tess");
		DocumentRecord document = new DocumentRecord("ankitmishra.me",
				"content", true, false, 100);
		DocumentRecordDA.putDocument(document);
		channel.addDocumentId("ankitmishra.me");
		channel.addXPath("/html");
		System.out.println(putChannel(channel));
		System.out.println(getChannel("peace"));
		System.out.println(XPathDA.getXPath("/html"));
		DBWrapper.closeDBWrapper();
	}*/
}
